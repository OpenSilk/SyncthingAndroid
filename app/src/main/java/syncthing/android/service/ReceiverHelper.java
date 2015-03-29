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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import timber.log.Timber;

/**
 * Created by drew on 3/29/15.
 */
public class ReceiverHelper {

    final Context appContext;

    public ReceiverHelper(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    public void setBootReceiverEnabled(boolean enabled) {
        if (enabled) {
            enableReceiver(BootReceiver.class);
        } else {
            disableReceiver(BootReceiver.class);
        }
    }

    public void setChargingReceiverEnabled(boolean enabled) {
        if (enabled) {
            enableReceiver(ChargingReceiver.class);
        } else {
            disableReceiver(ChargingReceiver.class);
        }
    }

    public void setConnectivityReceiverEnabled(boolean enabled) {
        if (enabled) {
            enableReceiver(ConnectivityReceiver.class);
        } else {
            disableReceiver(ConnectivityReceiver.class);
        }
    }

    public <T> void disableReceiver(Class<T> clzz) {
        Timber.d("disabling %s", clzz.getSimpleName());
        ComponentName cn = new ComponentName(appContext, clzz);
        PackageManager pm = appContext.getPackageManager();
        pm.setComponentEnabledSetting(cn,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    public <T> void enableReceiver(Class<T> clzz) {
        Timber.d("enabling %s", clzz.getSimpleName());
        ComponentName cn = new ComponentName(appContext, clzz);
        PackageManager pm = appContext.getPackageManager();
        pm.setComponentEnabledSetting(cn,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }
}
