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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by drew on 10/10/15.
 */
public class LogWriterThread extends Thread {
    final int type;
    final BufferedReader br;
    final AtomicReference<Process> goProcess;

    public LogWriterThread(int type, InputStream is, AtomicReference<Process> goProcess) {
        this.type = type;
        this.br = new BufferedReader(new InputStreamReader(is));
        this.goProcess = goProcess;
    }

    public static void initialize(int type, InputStream is, AtomicReference<Process> goProcess) {
        new LogWriterThread(type, is, goProcess).start();
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
        Timber.d("Running");
        while (goProcess.get() != null) {
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                Timber.w(e, "Unable to read log stream");
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
