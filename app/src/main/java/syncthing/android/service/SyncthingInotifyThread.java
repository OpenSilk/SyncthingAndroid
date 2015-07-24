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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        ConfigXml configXml = ConfigXml.get(mService);
        while (configXml == null) {
            Timber.d("Syncthing config does not exist yet, retrying in 30 seconds...");
            try { Thread.sleep(30000);
            } catch (InterruptedException e) {}
            configXml = ConfigXml.get(mService);
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
                new LogWriterThread(Log.INFO, p.getInputStream(), goProcess).start();
                new LogWriterThread(Log.ERROR, p.getErrorStream(), goProcess).start();
                ret = p.waitFor();
                Timber.d("syncthing-inotify exited with status %d", ret);
                goProcess.set(null);
                if (ret == 3) { //restart requested
                    Thread.sleep(100);
                }
            } catch (IOException e) {
                kill();
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                kill();
                throw new RuntimeException(e);
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
        try { p.waitFor();
        } catch (InterruptedException e) { }
        Timber.d("Syncthing-inotify killed ret=%d", p.exitValue());
    }

    static class LogWriterThread extends Thread {
        final int type;
        final BufferedReader br;
        final AtomicReference<Process> goProcess;

        LogWriterThread(int type, InputStream is, AtomicReference<Process> goProcess) {
            this.type = type;
            this.br = new BufferedReader(new InputStreamReader(is));
            this.goProcess = goProcess;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
            Timber.d("Running");
            while (true && goProcess.get() != null) {
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    Timber.w("Unable to read Syncthing-inotify's log", e);
                    break;
                }
                if (line == null) {
                    break;
                }
                if (type == Log.ERROR) {
                    Timber.e(line);
                } else {
                    Timber.i(line);
                }
            }
            Timber.d("Exiting");
        }
    }
}
