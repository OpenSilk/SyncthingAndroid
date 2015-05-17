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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.NotificationCompat;

import javax.inject.Inject;

import syncthing.android.R;
import syncthing.android.ui.LauncherActivity;

/**
 * Created by drew on 3/21/15.
 */
@SyncthingInstanceScope
public class NotificationHelper {

    public static final int SERVICE_NOTIFICATION = 1;

    final SyncthingInstance service;
    final NotificationManager notificationManager;

    @Inject
    public NotificationHelper(
            SyncthingInstance service,
            NotificationManager notificationManager
    ) {
        this.service = service;
        this.notificationManager = notificationManager;
    }


    void buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(service);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_cloud_circle_white_24dp)//TODO real icon
                .setLargeIcon(((BitmapDrawable) service.getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap())
                .setContentTitle(service.getString(R.string.syncthing_is_running))
                .setContentIntent(PendingIntent.getActivity(service, 0,
                        new Intent(service, LauncherActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        PendingIntent.FLAG_UPDATE_CURRENT)
                )
                //.setProgress(100, 100, false) //TODO
                .addAction(R.drawable.ic_close_grey600_24dp,
                        service.getResources().getString(R.string.shutdown),
                        PendingIntent.getService(service, 0,
                                new Intent(service, SyncthingInstance.class)
                                    .setAction(SyncthingInstance.SHUTDOWN),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )
                )
                ;
        service.startForeground(SERVICE_NOTIFICATION, builder.build());
    }

    void killNotification() {
        service.stopForeground(true);
    }
}
