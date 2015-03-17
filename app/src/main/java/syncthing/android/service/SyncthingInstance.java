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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import syncthing.android.BuildConfig;

/**
 * Created by drew on 3/8/15.
 */
public class SyncthingInstance extends Service {

    static final String PACKAGE = BuildConfig.APPLICATION_ID;

    public static final String ACTION_RESTART = PACKAGE + ".action.restart";
    public static final String ACTION_SHUTDOWN = PACKAGE + ".action.shutdown";
    public static final String ACTION_STATE_CHANGE = PACKAGE + ".action.statechange";
    public static final String ACTION_FOREGROUND_STATE_CHANGED = PACKAGE + ".action.fgstatechanged";

    public static final String EXTRA_STATE = PACKAGE + ".extra.state";
    public static final String EXTRA_NOW_IN_FOREGROUND = PACKAGE + ".extra.nowinforeground";

    private ISyncthingInstance mBinder;

    @Override
    public void onCreate() {
        mBinder = new SyncthingInstanceBinder(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder.asBinder();
    }

    /*
     * AIDL
     */

    public String getGuiAddress() {
        return null;
    }

    public String getApiKey() {
        return null;
    }

}
