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
import syncthing.api.model.DeviceStats;

/**
 * Created by drew on 3/10/15.
 */
public class DeviceCard extends ExpandableCard {

    protected final DeviceConfig device;
    protected ConnectionInfo connection;
    protected DeviceStats stats;
    protected int completion;

    public DeviceCard(DeviceConfig device, ConnectionInfo connection, DeviceStats stats, int completion) {
        this.device = device;
        this.connection = connection;
        this.stats = stats;
        this.completion = completion;
    }

    public void setConnectionInfo(ConnectionInfo connection) {
        this.connection = connection;
    }

    public void setDeviceStats(DeviceStats stats) {
        this.stats = stats;
    }

    public void setCompletion(int completion) {
        this.completion = completion;
    }

    @Override
    public int getLayout() {
        return R.layout.session_device;
    }

    @Override
    public int adapterId() {
        return super.adapterId() ^ device.deviceID.hashCode();
    }

}
