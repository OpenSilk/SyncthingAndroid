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

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import syncthing.android.BuildConfig;
import timber.log.Timber;

/**
 * Created by drew on 3/8/15.
 */
public class SyncthingInstance extends Service {

    static final String PACKAGE = BuildConfig.APPLICATION_ID;

    static final String NEED_RESTART = PACKAGE + ".action.needrestart";
    static final String WAS_SHUTDOWN = PACKAGE + ".action.wasshutdown";
    public static final String ACTION_RESTART = PACKAGE + ".action.restart";
    public static final String ACTION_SHUTDOWN = PACKAGE + ".action.shutdown";
    public static final String ACTION_STATE_CHANGE = PACKAGE + ".action.statechange";
    public static final String FOREGROUND_STATE_CHANGED = PACKAGE + ".action.fgstatechanged";

    public static final String EXTRA_STATE = PACKAGE + ".extra.state";
    public static final String EXTRA_NOW_IN_FOREGROUND = PACKAGE + ".extra.nowinforeground";

    ISyncthingInstance mBinder;
    SyncthingThread mSyncthingThread;

    int mConnectedClients = 0;
    boolean mAnyActivityInForeground;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate");
        ensureBinary();
        mBinder = new SyncthingInstanceBinder(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        ensureSyncthingKilled();
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
        boolean handled = false;
        if (intent != null) {
            String action = intent.getAction();

            if (intent.hasExtra(EXTRA_NOW_IN_FOREGROUND)) {
                mAnyActivityInForeground = intent.getBooleanExtra(EXTRA_NOW_IN_FOREGROUND, false);
                updateForegroundState();
            }

            if (WAS_SHUTDOWN.equals(action)) {
                doOrderlyShutdown();
                return START_NOT_STICKY;
            }

            if (NEED_RESTART.equals(action)) {
                ensureSyncthingKilled();
                startSyncthing();
                handled = true;
            }

        }
        if (!handled) {
            maybeStartSyncthing();
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

    void doOrderlyShutdown() {
        //TODO kill notification
        if (mConnectedClients == 0) {
            stopSelf();
        }
    }

    /*
     * Notification helpers
     */

    void updateForegroundState() {
        //TODO
    }

    /*
     * Syncthing Helpers
     */

    void startSyncthing() {
        mSyncthingThread = new SyncthingThread(this);
        mSyncthingThread.start();
    }

    void maybeStartSyncthing() {
        if (mSyncthingThread == null || !mSyncthingThread.isAlive()) {
            startSyncthing();
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
