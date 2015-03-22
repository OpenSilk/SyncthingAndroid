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
import android.os.IBinder;

import org.apache.commons.io.IOUtils;
import org.opensilk.common.mortar.MortarService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import mortar.MortarScope;
import mortar.dagger2support.DaggerService;
import syncthing.android.BuildConfig;
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
    //Reload settings
    public static final String REEVALUATE = PACKAGE + ".action.reevaluate";
    //shutdown service
    public static final String SHUTDOWN = PACKAGE + ".action.shutdown";
    //received be alarmmanager
    public static final String WAKEUP = PACKAGE + "action.wakeup";

    @Inject ServiceSettings mSettings;
    @Inject NotificationHelper mNotificationHelper;
    @Inject AlarmManagerHelper mAlarmManagerHelper;

    ISyncthingInstance mBinder;
    SyncthingThread mSyncthingThread;

    int mConnectedClients = 0;
    boolean mAnyActivityInForeground;

    @Override
    protected void onBuildScope(MortarScope.Builder builder) {
        builder.withService(DaggerService.SERVICE_NAME,
                DaggerService.createComponent(
                        SyncthingInstanceComponent.class,
                        DaggerService.getDaggerComponent(getApplicationContext()),
                        new SyncthingInstanceModule(this)
                )
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate");
        ensureBinary();
        DaggerService.<SyncthingInstanceComponent>getDaggerComponent(this).inject(this);
        mBinder = new SyncthingInstanceBinder(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        ensureSyncthingKilled();
        mAlarmManagerHelper.cancelDelayedShutdown();
        mAlarmManagerHelper.scheduleWakeup();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mConnectedClients++;
        return mBinder.asBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mConnectedClients--;
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand %s", intent);
        if (intent != null) {
            String action = intent.getAction();

            if (intent.hasExtra(EXTRA_NOW_IN_FOREGROUND)) {
                mAnyActivityInForeground = intent.getBooleanExtra(EXTRA_NOW_IN_FOREGROUND, false);
                updateForegroundState();
            }

            if (SHUTDOWN.equals(action)) {
                mAlarmManagerHelper.onReceivedDelayedShutdown();
                doOrderlyShutdown();
                return START_NOT_STICKY;
            }

            switch (action) {
                case BINARY_WAS_SHUTDOWN:
                    ensureSyncthingKilled();
                    mAlarmManagerHelper.scheduleDelayedShutdown();
                    break;
                case BINARY_NEED_RESTART:
                    safeStartSyncthing();
                    break;
                case REEVALUATE:
                case WAKEUP:
                default:
                    reevaluate();
                    break;
            }

        } else {
            //System restarted us
            reevaluate();
        }
        return START_STICKY;
    }

    /*
     * AIDL
     */

    public String getGuiAddress() {
        return null;
    }

    public String getApiKey() {
        return null;
    }

    void reevaluate() {
        if (mAnyActivityInForeground) {
            if (mSettings.isDisabled()) {
                ensureSyncthingKilled();
                doOrderlyShutdown();
            } else if (mSettings.hasSuitableConnection()) {
                maybeStartSyncthing();
            } else {
                mAlarmManagerHelper.scheduleDelayedShutdown();
            }
        } else {
            //in background
            if (mSettings.isAllowedToRun()) {
                //as you were
                maybeStartSyncthing();
                if (mSettings.isOnSchedule()) {
                    //TODO always set this and dont cancel when
                    //receive updates from binary
                    mAlarmManagerHelper.scheduleDelayedShutdown();
                }
            } else {
                //no foreground and not allowed to run
                ensureSyncthingKilled();
                mAlarmManagerHelper.scheduleDelayedShutdown();
            }
        }
    }

    void doOrderlyShutdown() {
        mNotificationHelper.killNotification();
        if (mConnectedClients == 0) {
            stopSelf();
        }
    }

    /*
     * Notification helpers
     */

    void updateForegroundState() {
        if (mAnyActivityInForeground) {
            mNotificationHelper.killNotification();
        } else {
            mNotificationHelper.buildNotification();
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
    }

    void maybeStartSyncthing() {
        if (!isSyncthingRunning()) {
            safeStartSyncthing();
        }
    }

    void ensureSyncthingKilled() {
        if (mSyncthingThread != null) {
            mSyncthingThread.kill();
            mSyncthingThread = null;
        }
    }

    /*
     * Initial setup
     */

    // From camlistore
    void ensureBinary() {
        long myTime = getAPKModTime();
        String dstFile = getBaseContext().getFilesDir().getAbsolutePath() + "/syncthing.bin";
        File f = new File(dstFile);
        Timber.d("My Time: %d", myTime);
        Timber.d("Bin Time: " + f.lastModified());
        if (f.exists() && f.lastModified() > myTime) {
            Timber.d("Go binary modtime up-to-date.");
            return;
        }
        Timber.d("Go binary missing or modtime stale. Re-copying from APK.");
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = getAssets().open("syncthing.arm");
            fos = getBaseContext().openFileOutput("syncthing.bin.writing", MODE_PRIVATE);
            IOUtils.copy(is, fos);
            fos.flush();

            String writingFilePath = dstFile + ".writing";
            Timber.d("wrote out %s", writingFilePath);
            Runtime.getRuntime().exec("chmod 0777 " + writingFilePath);
            Timber.d("did chmod 0700 on %s", writingFilePath);
            Runtime.getRuntime().exec("mv " + writingFilePath + " " + dstFile);
            Timber.d("moved %s to %s", writingFilePath, dstFile);
            f = new File(dstFile);
            f.setLastModified(myTime);
            Timber.d("set modtime of %s", dstFile);
        } catch (IOException e) {
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

}
