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

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by drew on 3/19/15.
 */
public class SyncthingInotifyThread extends Thread {

    final AtomicReference<Process> goProcess = new AtomicReference<>();
    final SyncthingInstance mService;

    public SyncthingInotifyThread(SyncthingInstance mService) {
        this.mService = mService;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
        if (!mService.getSettings().isInitialised()) {
            Timber.w("Syncthing not initialized yet... Quiting");
            return;
        }
        realRun();
    }

    void realRun() {
        Timber.d("Running");
        int ret = 3;
        while (ret == 3) {
            try {
                ProcessBuilder b = new ProcessBuilder();
                b.command(SyncthingUtils.getSyncthingInotifyBinaryPath(mService),
                        "-home", SyncthingUtils.getConfigDirectory(mService).getAbsolutePath()
                        //, "-verbosity", "5"
                );
                Process p = b.start();
                goProcess.set(p);
                LogWriterThread.initialize(Log.INFO, p.getInputStream(), goProcess);
                LogWriterThread.initialize(Log.ERROR, p.getErrorStream(), goProcess);
                ret = p.waitFor();
                Timber.d("syncthing-inotify exited with status %d", ret);
                goProcess.set(null);
                if (ret == 3) { //restart requested
                    Thread.sleep(100);
                }
            } catch (IOException|InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                kill();
            }
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
            Timber.d("Syncthing-inotify killed ret=%d", p.exitValue());
        } catch (InterruptedException|IllegalThreadStateException e) {
            Timber.e(e, "Error killing syncthing-inotify");
        }
    }
}
