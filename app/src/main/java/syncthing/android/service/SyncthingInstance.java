/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package syncthing.android.service;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.core.mortar.MortarService;
import org.opensilk.common.core.util.VersionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import mortar.MortarScope;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import syncthing.android.BuildConfig;
import syncthing.api.Session;
import syncthing.api.SessionController;
import syncthing.api.SessionManager;
import syncthing.api.SynchingApiWrapper;
import syncthing.api.SyncthingApi;
import syncthing.api.SyncthingApiConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.Ok;
import syncthing.api.model.event.ItemFinished;
import timber.log.Timber;

/**
 * Created by drew on 3/8/15.
 */
public class SyncthingInstance extends MortarService {

    static final String PACKAGE = BuildConfig.APPLICATION_ID;

    //activity is informing us it was opened or closed
    public static final String FOREGROUND_STATE_CHANGED = PACKAGE + ".action.fgstatechanged";
    public static final String EXTRA_NOW_IN_FOREGROUND = PACKAGE + ".extra.nowinforeground";
    //binary exited with restart status
    static final String BINARY_NEED_RESTART = PACKAGE + ".action.binaryneedrestart";
    //binary exited with clean shutdown status
    static final String BINARY_WAS_SHUTDOWN = PACKAGE + ".action.binarywasshutdown";
    //binary exited with unhandled exit code
    static final String BINARY_DIED = PACKAGE + ".action.binarydied";
    //Reload settings
    public static final String REEVALUATE = PACKAGE + ".action.reevaluate";
    //shutdown service
    public static final String SHUTDOWN = PACKAGE + ".action.shutdown";
    //recieved by alarmmanager
    static final String SCHEDULED_SHUTDOWN = PACKAGE + ".action.scheduledshutdown";
    static final String SCHEDULED_WAKEUP = PACKAGE + "action.wakeup";

    @Inject ServiceSettings mSettings;
    @Inject NotificationHelper mNotificationHelper;
    @Inject AlarmManagerHelper mAlarmManagerHelper;
    @Inject SessionManager mSessionManager;
    @Inject WifiManager mWifiManager;
    @Inject ConnectivityManager mConnectivityManager;

    SyncthingThread mSyncthingThread;
    SyncthingInotifyThread mSyncthingInotifyThread;

    WifiManager.WifiLock mWifiLock;

    ContentObserver initializedObserver;
    Session mSession;
    final SessionHelper mSessionHelper = new SessionHelper();

    boolean mAnyActivityInForeground;
    boolean wasShutdown;

    static class SessionHelper {
        Subscription eventSubscripion;
        void release() {
            if (eventSubscripion != null) {
                eventSubscripion.unsubscribe();
            }
        }
    }

    @Override
    protected void onBuildScope(MortarScope.Builder builder) {
        ServiceComponent component = DaggerService.getDaggerComponent(getApplicationContext());
        builder.withService(DaggerService.DAGGER_SERVICE,
                SyncthingInstanceComponent.FACTORY.call(component, this));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate");
        ensureBinaries();
        DaggerService.<SyncthingInstanceComponent>getDaggerComponent(this).inject(this);
        mSettings.setCached(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        ensureSyncthingKilled();
        mAlarmManagerHelper.cancelDelayedShutdown();
        mSettings.release();
        if (initializedObserver != null) {
            getContentResolver().unregisterContentObserver(initializedObserver);
        }
        releaseSession();
        releaseWifiLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("service not bindable");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand %s", intent);
        if (intent != null) {
            String action = intent.getAction();

            if (intent.hasExtra(EXTRA_NOW_IN_FOREGROUND)) {
                mAnyActivityInForeground = intent.getBooleanExtra(EXTRA_NOW_IN_FOREGROUND, false);
                if (!mAnyActivityInForeground && wasShutdown) {
                    doOrderlyShutdown();
                    return START_NOT_STICKY;
                }
            } else {
                wasShutdown = false;
            }

            if (SHUTDOWN.equals(action) || SCHEDULED_SHUTDOWN.equals(action)) {
                if (SCHEDULED_SHUTDOWN.equals(action)) {
                    mAlarmManagerHelper.onReceivedDelayedShutdown();
                }
                doOrderlyShutdown();
                return START_NOT_STICKY;
            }

            switch (action) {
                case BINARY_DIED:
                    tryKillingRougeInstance();
                    doOrderlyShutdown();
                    mNotificationHelper.showError();
                    break;
                case BINARY_WAS_SHUTDOWN:
                    doOrderlyShutdown();
                    break;
                case BINARY_NEED_RESTART:
                    safeStartSyncthing();
                    updateForegroundState();
                    break;
                case REEVALUATE:
                case SCHEDULED_WAKEUP:
                default:
                    reevaluate();
                    break;
            }

        } else { //System restarted us
            reevaluate();
        }

        return START_STICKY;
    }

    void reevaluate() {
        if (mAnyActivityInForeground) {
            //when in foreground we only care about disabled status and
            //connection override, we will assume that since the user
            //opened the app they wish for the server to start
            if (mSettings.isDisabled() || !mSettings.hasSuitableConnection()) {
                ensureSyncthingKilled();
            } else {
                maybeStartSyncthing();
                mAlarmManagerHelper.cancelDelayedShutdown();
            }
        } else {
            //in background
            if (mSettings.isAllowedToRun()) {
                maybeStartSyncthing(); //as you were
                if (mSettings.isOnSchedule()) {
                    mAlarmManagerHelper.scheduleDelayedShutdown();
                    mAlarmManagerHelper.scheduleWakeup();
                } else /*always run*/ {
                    mAlarmManagerHelper.cancelDelayedShutdown();
                }
                if (isConnectedToWifi()) {
                    acquireWifiLock();
                } else {
                    releaseWifiLock();
                }
            } else {
                ensureSyncthingKilled();
                //dont shutdown right away in case circumstances change
                mAlarmManagerHelper.scheduleDelayedShutdown();
                if (mSettings.isOnSchedule()) {
                    mAlarmManagerHelper.scheduleWakeup();
                }
                releaseWifiLock();
            }
        }
        updateForegroundState();
    }

    void doOrderlyShutdown() {
        wasShutdown = true;
        ensureSyncthingKilled();
        mNotificationHelper.killNotification();
        //always stick around while activity is running
        if (!mAnyActivityInForeground) {
            stopSelf();
        }
    }

    /*
     * Notification helpers
     */

    void updateForegroundState() {
        if (isSyncthingRunning()) {
            //show if server running
            mNotificationHelper.buildNotification();
        } else {
            //server not running dont show
            mNotificationHelper.killNotification();
        }
    }

    /*
     * Syncthing Helpers
     */

    boolean isSyncthingRunning() {
        return mSyncthingThread != null && mSyncthingThread.isAlive();
    }

    void safeStartSyncthing() {
        ensureSyncthingKilled();
        startSyncthing();
    }

    void startSyncthing() {
        mSyncthingThread = new SyncthingThread(this);
        mSyncthingThread.start();
        if (mSettings.isInitialised()) {
            startInotify();
        } else if (initializedObserver == null) {
            //register listener and wait for notify
            initializedObserver = new InitializedListener(this);
            getContentResolver().registerContentObserver(
                    mSettings.getInitializedUri(),
                    false, initializedObserver);
        } //else already listening
    }

    void maybeStartSyncthing() {
        if (!isSyncthingRunning()) {
            safeStartSyncthing();
        }
    }

    void ensureSyncthingKilled() {
        releaseSession();
        ensureInotifyKilled();
        if (mSyncthingThread != null) {
            mSyncthingThread.kill();
            mSyncthingThread = null;
        }
    }

    boolean isInotifyRunning() {
        return mSyncthingInotifyThread != null && mSyncthingInotifyThread.isAlive();
    }

    void safeStartInotify() {
        ensureInotifyKilled();
        startInotify();
    }

    void startInotify() {
        mSyncthingInotifyThread = new SyncthingInotifyThread(this);
        mSyncthingInotifyThread.start();
        startMonitor();
    }

    void maybeStartInotify() {
        if (isSyncthingRunning() && !isInotifyRunning()) {
            safeStartInotify();
        }
    }

    void ensureInotifyKilled() {
        if (mSyncthingInotifyThread != null) {
            mSyncthingInotifyThread.kill();
            mSyncthingInotifyThread = null;
        }
    }

    void releaseSession() {
        if (mSession != null) {
            mSessionManager.release(mSession);
        }
        mSessionHelper.release();
    }

    void acquireSession() {
        SyncthingApiConfig.Builder bob = SyncthingApiConfig.builder();
        ConfigXml config = ConfigXml.get(this);
        //noinspection ConstantConditions
        bob.setUrl(config.getUrl());
        bob.setApiKey(config.getApiKey());
        bob.setCaCert(SyncthingUtils.getSyncthingCACert(this));
        releaseSession();
        mSession = mSessionManager.acquire(bob.build());
    }

    void startMonitor() {
        acquireSession();
        final SessionController controller = mSession.controller();
        controller.init();
        mSessionHelper.eventSubscripion =
        controller.subscribeChanges(new Action1<SessionController.ChangeEvent>() {
            @Override
            public void call(SessionController.ChangeEvent changeEvent) {
                switch (changeEvent.change) {
                    case ONLINE: {
                        break;
                    }
                    case ITEM_FINISHED: {
                        ItemFinished.Data data = (ItemFinished.Data) changeEvent.data;
                        switch (data.action) {
                            case UPDATE: {
                                FolderConfig folder = controller.getFolder(data.folder);
                                if (folder != null) {
                                    File file = new File(folder.path, data.item);
                                    Timber.d("Item finished update for %s", file.getAbsolutePath());
                                    if (file.exists()) {
                                        MediaScannerConnection.scanFile(SyncthingInstance.this,
                                                new String[]{file.getAbsolutePath()}, null, null);
                                    }
                                }
                                break;
                            }
                            case DELETE: {
                                FolderConfig folder = controller.getFolder(data.folder);
                                if (folder != null) {
                                    File file = new File(folder.path, data.item);
                                    Timber.d("Item finished delete for %s", file.getAbsolutePath());
                                    if (!file.exists()) {
                                        final String where = MediaStore.Files.FileColumns.DATA + "=?";
                                        int count = getContentResolver().delete(MediaStore.Files.getContentUri("external"),
                                            where, new String[] {file.getAbsolutePath()});
                                        Timber.i("Removed %d items from mediastore for path %s",
                                                count, file.getAbsolutePath());
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }, SessionController.Change.ONLINE, SessionController.Change.ITEM_FINISHED);
    }

    void tryKillingRougeInstance() {
        acquireSession();
        try {
            Ok ok = SynchingApiWrapper.wrap(mSession.api(), Schedulers.newThread())
                    .shutdown().timeout(2, TimeUnit.SECONDS).toBlocking().first();
        } catch (RuntimeException ignored) {
        }
    }

    public ServiceSettings getSettings() {
        return mSettings;
    }

    boolean isConnectedToWifi() {
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIMAX;
    }

    void acquireWifiLock() {
        releaseWifiLock();
        mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "SyncthingInstance");
        mWifiLock.acquire();
    }

    void releaseWifiLock() {
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
            mWifiLock = null;
        }
    }

    /*
     * Initial setup
     */

    void ensureBinaries() {
        final String[] abis;
        if (VersionUtils.hasLollipop()) {
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
        String ext = null;
        for (String abi: abis) {
            if (StringUtils.equals(abi, "x86_64")) {
                Timber.i("Found abi %s", abi);
                ext = "amd64";
                break;
            } else if (StringUtils.equals(abi, "x86")) {
                Timber.i("Found abi %s", abi);
                ext = "386";
                break;
            } else if (StringUtils.equals(abi, "armeabi-v7a")) {
                Timber.i("Found abi %s", abi);
                ext = "arm";
                break;
            }
        }
        if (ext == null) throw new RuntimeException("Unable to find supported arch in " + Arrays.toString(abis));
        ensureBinary("syncthing" + "." + ext, SyncthingUtils.getSyncthingBinaryPath(this));
        ensureBinary("syncthing-inotify" + "." + ext, SyncthingUtils.getSyncthingInotifyBinaryPath(this));
    }

    // From camlistore
    void ensureBinary(String asset, String destPath) {
        long myTime = getAPKModTime();
        File f = new File(destPath);
        Timber.d("My Time:  %d", myTime);
        Timber.d("Bin Time: %d", f.lastModified());
        if (f.exists() && f.lastModified() > myTime) {
            Timber.i("%s modtime up-to-date.", f.getName());
            return;
        }
        Timber.i("%s missing or modtime stale. Re-copying from APK.", f.getName());
        String writingFilePath = destPath + ".writing";
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = getAssets().open(asset);
            fos = new FileOutputStream(new File(writingFilePath));
            IOUtils.copy(is, fos);
            fos.flush();
            Timber.d("wrote out %s", writingFilePath);
            Runtime.getRuntime().exec("chmod 0700 " + writingFilePath).waitFor();
            Timber.d("did chmod 0700 on %s", writingFilePath);
            Runtime.getRuntime().exec("mv " + writingFilePath + " " + destPath).waitFor();
            Timber.d("moved %s to %s", writingFilePath, destPath);
            f = new File(destPath);
            if (f.setLastModified(System.currentTimeMillis())) {
                Timber.d("set modtime of %s", destPath);
            }
        } catch (IOException|InterruptedException e) {
            FileUtils.deleteQuietly(new File(destPath));
            FileUtils.deleteQuietly(new File(writingFilePath));
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(fos);
        }
    }

    long getAPKModTime() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static class InitializedListener extends ContentObserver {
        WeakReference<SyncthingInstance> mService;
        public InitializedListener(SyncthingInstance service) {
            super(new Handler(Looper.getMainLooper()));
            mService = new WeakReference<SyncthingInstance>(service);
        }

        @Override
        public void onChange(boolean selfChange) {
            SyncthingInstance s = mService.get();
            if (s != null && s.mSettings.isInitialised()) {
                s.maybeStartInotify();
            }
        }
    }

}
