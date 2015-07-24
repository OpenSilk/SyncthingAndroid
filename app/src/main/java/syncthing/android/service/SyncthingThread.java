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
            configXml.changeDefaultFolder();
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
            new LogWriterThread(Log.INFO, p.getInputStream(), goProcess).start();
            new LogWriterThread(Log.ERROR, p.getErrorStream(), goProcess).start();
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
        } catch (IOException e) {
            kill();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            kill();
            throw new RuntimeException(e);
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
        int retries = 60;
        for (int i = 0; i < retries; i++) {
            try {
                int exitValue = p.exitValue();
                Timber.d("Syncthing killed ret=%d", exitValue);
                Thread.sleep(100);
                return;
            } catch (InterruptedException|IllegalThreadStateException e) { }
        }
        killAlternative();
    }

    private void killAlternative() {
        Timber.d("killAlternative");
        // Ensure kill
        for (int i = 0; i < 2; i++) {
            Process ps = null;
            DataOutputStream psOut = null;
            try {
                ps = Runtime.getRuntime().exec("sh");
                psOut = new DataOutputStream(ps.getOutputStream());
                psOut.writeBytes("ps | grep libsyncthing.so\n");
                psOut.writeBytes("exit\n");
                psOut.flush();
                ps.waitFor();
                InputStreamReader isr = new InputStreamReader(ps.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String id;
                while ((id = br.readLine()) != null) {
                    killProcessId(id, i > 0);
                }
            } catch (IOException | InterruptedException e) {
                Timber.e("No Syncthing processes found", e);
            } finally {
                try {
                    if (psOut != null)
                        psOut.close();
                } catch (IOException e) {
                    Timber.e("Failed close the psOut stream", e);
                }
                if (ps != null) {
                    ps.destroy();
                }
            }
        }
    }

    /**
     * Kill a given process ID
     *
     * @param force Whether to use a SIGKILL.
     */
    void killProcessId(String id, boolean force) {
        Process kill = null;
        DataOutputStream killOut = null;
        try {
            kill = Runtime.getRuntime().exec("sh");
            killOut = new DataOutputStream(kill.getOutputStream());
            if (!force) {
                killOut.writeBytes("kill " + id + "\n");
                killOut.writeBytes("sleep 1\n");
            } else {
                killOut.writeBytes("sleep 3\n");
                killOut.writeBytes("kill -9 " + id + "\n");
            }
            killOut.writeBytes("exit\n");
            killOut.flush();
            kill.waitFor();
            Timber.d("Killed Syncthing process " + id);
        } catch (IOException | InterruptedException e) {
            Timber.e("Failed to kill process id " + id, e);
        } finally {
            try {
                if (killOut != null)
                    killOut.close();
            } catch (IOException e) {
                Timber.e("Failed close the killOut stream", e);}
            if (kill != null) {
                kill.destroy();
            }
        }
        Timber.d("Syncthing killed ret=%d", kill.exitValue());
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
                    Timber.w("Unable to read Syncthing's log", e);
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
