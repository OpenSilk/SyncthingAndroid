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
import android.app.NotificationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import org.opensilk.common.core.dagger2.AppContextComponent;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import syncthing.android.AppModule;
import syncthing.api.GsonModule;
import syncthing.api.SessionManagerComponent;

/**
 * Created by drew on 3/21/15.
 */
@Singleton
@Component(
        modules = {
                AppModule.class,
                GsonModule.class,
                ServiceSettingsModule.class
        }
)
public interface ServiceComponent extends SessionManagerComponent, AppContextComponent {
    NotificationManager notificationManager();
    AlarmManager alarmManager();
    WifiManager wifiManager();
    ConnectivityManager connectivityManager();
    @Named("settingsAuthority") String settingsAuthority();
    void inject(ServiceSettingsProvider provider);
}
