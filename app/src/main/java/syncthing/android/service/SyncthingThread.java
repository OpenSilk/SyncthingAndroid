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
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by drew on 3/19/15.
 */
public class SyncthingThread extends Thread {

    final AtomicReference<Process> goProcess = new AtomicReference<>();
    final SyncthingInstance mService;

    public SyncthingThread(SyncthingInstance mService) {
        this.mService = mService;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
        ConfigXml configXml = ConfigXml.get(mService);
        if (configXml == null) {
            Timber.d("first run: generating keys...");
            //we dont actually need to do this, its more of a hack
            //to sneak the config changes in.
            realRun(true);
            configXml = ConfigXml.get(mService);
            if (configXml == null) {
                Timber.e("Failed to generate config");
                return;
            }
            configXml.changeDefaultGUIAddress();
            configXml.changeDefaultFolder();
            configXml.changeDefaultDeviceName();
            mService.getSettings().setInitialized(true);
        }
        configXml.updateIfNeeded();
        realRun(false);
    }

    void realRun(boolean generate) {
        Timber.d("Running");
        int ret = 0;
        try {
            ProcessBuilder b = new ProcessBuilder();
            b.environment().put("HOME", Environment.getExternalStorageDirectory().getAbsolutePath());
            if (generate) {
                b.command(SyncthingUtils.getSyncthingBinaryPath(mService),
                        "-home", SyncthingUtils.getConfigDirectory(mService).getAbsolutePath(),
                        "-generate", SyncthingUtils.getConfigDirectory(mService).getAbsolutePath(),
                        "-no-restart",
                        "-no-browser"
                );
            } else {
                b.command(SyncthingUtils.getSyncthingBinaryPath(mService),
                        "-home", SyncthingUtils.getConfigDirectory(mService).getAbsolutePath(),
                        "-no-restart",
                        "-no-browser"
                );
            }
            Process p = b.start();
            goProcess.set(p);
            LogWriterThread.initialize(Log.INFO, p.getInputStream(), goProcess);
            LogWriterThread.initialize(Log.ERROR, p.getErrorStream(), goProcess);
            ret = p.waitFor();
            Timber.d("Syncthing exited with status %d", ret);
            goProcess.set(null);
            if (!generate) {
                if (ret == 3) { //restart requested
                    mService.startService(new Intent(mService, SyncthingInstance.class)
                            .setAction(SyncthingInstance.BINARY_NEED_RESTART));
                } else if (ret == 0 || ret == 1) { //shutdown
                    mService.startService(new Intent(mService, SyncthingInstance.class)
                            .setAction(SyncthingInstance.BINARY_WAS_SHUTDOWN));
                }
            }
        } catch (IOException|InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            kill();
        }
        Timber.d("Exiting");
    }

    public void kill() {
        Timber.d("kill");
        final Process p = goProcess.get();
        if (p == null) {
            return;
        }
        goProcess.set(null);
        p.destroy();
        try {
            p.waitFor();
            Timber.d("Syncthing killed ret=%d", p.exitValue());
        } catch (InterruptedException|IllegalThreadStateException e) {
            Timber.e(e, "Error killing syncthing");
        }
    }

}
