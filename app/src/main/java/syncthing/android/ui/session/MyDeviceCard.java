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

package syncthing.android.ui.session;

import syncthing.android.R;
import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.ExpandableCard;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;

/**
 * Created by drew on 3/10/15.
 */
public class MyDeviceCard extends ExpandableCard {

    protected final DeviceConfig device;
    protected ConnectionInfo connection;
    protected SystemInfo system;
    protected final Version version;

    public MyDeviceCard(DeviceConfig device, ConnectionInfo connection, SystemInfo system, Version version) {
        this.device = device;
        this.connection = connection;
        this.system = system;
        this.version = version;
    }

    public void setConnectionInfo(ConnectionInfo connection) {
        this.connection = connection;
    }

    void setSystemInfo(SystemInfo system) {
        this.system = system;
    }

    @Override
    public int getLayout() {
        return R.layout.session_mydevice;
    }

    @Override
    public int adapterId() {
        return super.adapterId() ^ device.deviceID.hashCode();
    }
}
