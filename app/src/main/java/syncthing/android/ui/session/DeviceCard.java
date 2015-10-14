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

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.support.v4.content.ContextCompat;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ExpandableCard;
import syncthing.api.model.Compression;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.DeviceStats;

/**
 * Created by drew on 3/10/15.
 */
public class DeviceCard extends ExpandableCard {

    protected DeviceConfig device;
    protected ConnectionInfo connection;
    protected DeviceStats stats;
    protected int completion;

    public DeviceCard(DeviceConfig device) {
        this.device = device;
    }

    public DeviceCard(DeviceConfig device, ConnectionInfo connection, DeviceStats stats, int completion) {
        this.device = device;
        this.connection = connection;
        this.stats = stats;
        this.completion = completion;
    }

    public void setDevice(DeviceConfig device) {
        if (!StringUtils.equals(this.device.deviceID, device.deviceID)) {
            throw new IllegalArgumentException("Tried binding a different device to this card " +
                    device.deviceID +" != " + this.device.deviceID);
        }
        this.device = device;
        notifyChange(syncthing.android.BR._all);//TODO only notify changed fields
    }

    public void setConnectionInfo(ConnectionInfo connection) {
        if (this.connection == null || connection == null) {
            this.connection = connection;
            notifyChange(syncthing.android.BR.inbps);
            notifyChange(syncthing.android.BR.inBytesTotal);
            notifyChange(syncthing.android.BR.outbps);
            notifyChange(syncthing.android.BR.outBytesTotal);
            notifyChange(syncthing.android.BR.connected);
        } else {
            if (this.connection.inbps != connection.inbps) {
                this.connection.inbps = connection.inbps;
                notifyChange(syncthing.android.BR.inbps);
            }
            if (this.connection.inBytesTotal != connection.inBytesTotal) {
                this.connection.inBytesTotal = connection.inBytesTotal;
                notifyChange(syncthing.android.BR.inBytesTotal);
            }
            if (this.connection.outbps != connection.outbps) {
                this.connection.outbps = connection.outbps;
                notifyChange(syncthing.android.BR.inBytesTotal);
            }
            if (this.connection.outBytesTotal != connection.outBytesTotal) {
                this.connection.outBytesTotal = connection.outBytesTotal;
                notifyChange(syncthing.android.BR.inBytesTotal);
            }
        }
    }

    public void setDeviceStats(DeviceStats stats) {
        this.stats = stats;
        notifyChange(syncthing.android.BR.lastSeen);
    }

    public void setCompletion(int completion) {
        this.completion = completion;
        notifyChange(syncthing.android.BR.completion);
    }

    @Override
    public int getLayout() {
        return R.layout.session_device;
    }

    @Bindable
    public String getDeviceID() {
        return device.deviceID;
    }

    @Bindable
    public String getName() {
        return SyncthingUtils.getDisplayName(device);
    }

    @Bindable
    public DateTime getLastSeen() {
        return stats != null ? stats.lastSeen : null;
    }

    @Bindable
    public int getCompletion() {
        return completion;
    }

    @Bindable
    public Compression getCompression() {
        return device.compression;
    }

    @Bindable
    public boolean getIntroducer() {
        return device.introducer;
    }

    @Bindable
    public long getInbps() {
        return connection != null ? connection.inbps : -1;
    }

    @Bindable
    public long getInBytesTotal(){
        return connection != null ? connection.inBytesTotal : -1;
    }

    @Bindable
    public long getOutbps() {
        return connection != null ? connection.outbps : -1;
    }

    @Bindable
    public long getOutBytesTotal() {
        return connection != null ? connection.outBytesTotal : -1;
    }

    @Bindable
    public String getAddress() {
        return connection != null ? connection.address : null;
    }

    @Bindable
    public String getClientVersion() {
        return connection != null ? connection.clientVersion : "?";
    }

    @Bindable
    public boolean isConnected() {
        return connection != null;
    }

    static final DateTime epoch = new DateTime(1969);
    @BindingAdapter("deviceLastSeenText")
    public static void deviceLastSeenText(TextView view, DateTime lastSeen) {
        if (lastSeen == null) {
            view.setText(R.string.unknown);
        } else {
            if (lastSeen.year().equals(epoch.year())) {
                view.setText(R.string.never);
            } else {
                view.setText(lastSeen.toString("yyyy-MM-dd HH:mm"));
            }
        }
    }

    @BindingAdapter("deviceCompletion")
    public static void deviceCompletion(TextView view, int completion) {
        if (completion < 0) {
            view.setText(R.string.disconnected);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.device_disconnected));
        } else if (completion == 100) {
            view.setText(R.string.up_to_date);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.device_idle));
        } else {
            view.setText(R.string.syncing);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.device_syncing));
        }
    }

}
