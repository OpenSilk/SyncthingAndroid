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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.SystemClock;

import org.joda.time.DateTime;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by drew on 3/21/15.
 */
@SyncthingInstanceScope
public class AlarmManagerHelper {

    static final int KEEP_ALIVE = 2 * 60 * 1000;//2min

    final SyncthingInstance service;
    final AlarmManager alarmManager;
    final ServiceSettings settings;

    private boolean shudownScheduled;
    PendingIntent shutdownIntent;
    PendingIntent wakeupIntent;

    @Inject
    public AlarmManagerHelper(
            SyncthingInstance service,
            AlarmManager alarmManager,
            ServiceSettings settings
    ) {
        this.service = service;
        this.alarmManager = alarmManager;
        this.settings = settings;
    }

    void scheduleDelayedShutdown() {
        cancelDelayedShutdown();
        if (shutdownIntent == null) {
            Intent intent = new Intent(service, SyncthingInstance.class).setAction(SyncthingInstance.SCHEDULED_SHUTDOWN);
            shutdownIntent = PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        if (settings.isOnSchedule()) {
            long nextShutdown = settings.getNextScheduledEndTime();
            Timber.d("Scheduling shutdown at %s", new DateTime(nextShutdown).toString());
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextShutdown, shutdownIntent);
        } else {
            long nextStutdown = SystemClock.elapsedRealtime()+KEEP_ALIVE;
            Timber.d("Scheduling shutdown in %dms", KEEP_ALIVE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextStutdown, shutdownIntent);
        }
        shudownScheduled = true;
    }

    void cancelDelayedShutdown() {
        if (shudownScheduled) {
            shudownScheduled = false;
            alarmManager.cancel(shutdownIntent);
        }
    }

    void onReceivedDelayedShutdown() {
        shudownScheduled = false;
    }

    void scheduleWakeup() {
        if (wakeupIntent == null) {
            Intent intent = new Intent(service, SyncthingInstance.class).setAction(SyncthingInstance.SCHEDULED_WAKEUP);
            wakeupIntent = PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        if (settings.isOnSchedule()) {
            long nextWakeup = settings.getNextScheduledStartTime();
            Timber.d("Scheduling wakeup at %s", new DateTime(nextWakeup).toString());
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextWakeup, wakeupIntent);
        }
    }

}
