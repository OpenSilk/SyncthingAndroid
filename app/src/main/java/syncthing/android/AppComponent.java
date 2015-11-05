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

package syncthing.android;

import android.content.Context;
import android.net.wifi.WifiManager;

import org.opensilk.common.core.dagger2.ForApplication;

import javax.inject.Singleton;

import dagger.Component;
import syncthing.android.identicon.IdenticonComponent;
import syncthing.android.identicon.IdenticonModule;
import syncthing.android.service.ServiceSettings;
import syncthing.android.service.ServiceSettingsModule;
import syncthing.android.settings.AppSettings;
import syncthing.api.GsonModule;
import syncthing.api.SessionManagerComponent;

/**
 * Created by drew on 3/4/15.
 */
@Singleton
@Component (
        modules = {
                AppModule.class,
                GsonModule.class,
                IdenticonModule.class,
                ServiceSettingsModule.class
        }
)
public interface AppComponent extends SessionManagerComponent, IdenticonComponent {
    String NAME = AppComponent.class.getName();
    @ForApplication Context appContext();
    AppSettings appSettings();
    ServiceSettings serviceSettings();
    WifiManager wifimanager();
}
