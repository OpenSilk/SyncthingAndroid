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
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;

import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ExpandableCard;
import syncthing.api.model.Compression;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.DeviceStats;
import timber.log.Timber;

/**
 * Created by drew on 3/10/15.
 */
public class DeviceCard extends ExpandableCard {

    private final SessionPresenter presenter;
    protected DeviceConfig device;
    protected DeviceStats stats;
    protected int completion = -1;

    private long inbps = -1;
    private long inBytesTotal = -1;
    private long outbps = -1;
    private long outBytesTotal = -1;
    private String address;
    private String clientVersion = "?";
    private boolean connected;
    private boolean paused;

    public DeviceCard(
            SessionPresenter presenter,
            DeviceConfig device,
            ConnectionInfo connection,
            DeviceStats stats,
            int completion
    ) {
        this.presenter = presenter;
        this.device = device;
        this.stats = stats;
        this.completion = completion;
        setConnectionInfo(connection);
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
        Timber.d("setConnectionInfo(%s)", ReflectionToStringBuilder.toString(connection));
        if (connection == null) {
            connected = false;
            notifyChange(syncthing.android.BR.inbps);
            notifyChange(syncthing.android.BR.inBytesTotal);
            notifyChange(syncthing.android.BR.outbps);
            notifyChange(syncthing.android.BR.outBytesTotal);
            notifyChange(syncthing.android.BR.address);
            notifyChange(syncthing.android.BR.clientVersion);
            notifyChange(syncthing.android.BR.connected);
            notifyChange(syncthing.android.BR.paused);
        } else {
            if (inbps != connection.inbps) {
                inbps = connection.inbps;
                notifyChange(syncthing.android.BR.inbps);
            }
            if (inBytesTotal != connection.inBytesTotal) {
                inBytesTotal = connection.inBytesTotal;
                notifyChange(syncthing.android.BR.inBytesTotal);
            }
            if (outbps != connection.outbps) {
                outbps = connection.outbps;
                notifyChange(syncthing.android.BR.outbps);
            }
            if (outBytesTotal != connection.outBytesTotal) {
                outBytesTotal = connection.outBytesTotal;
                notifyChange(syncthing.android.BR.outBytesTotal);
            }
            if (!StringUtils.equals(address, connection.address)) {
                address = connection.address;
                notifyChange(syncthing.android.BR.address);
            }
            if (!StringUtils.equals(clientVersion, connection.clientVersion)) {
                clientVersion = connection.clientVersion;
                notifyChange(syncthing.android.BR.clientVersion);
            }
            if (connected != connection.connected) {
                connected = connection.connected;
                notifyChange(syncthing.android.BR.connected);
            }
            if (paused != connection.paused) {
                paused = connection.paused;
                notifyChange(syncthing.android.BR.paused);
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
        return inbps;
    }

    @Bindable
    public long getInBytesTotal(){
        return inBytesTotal;
    }

    @Bindable
    public long getOutbps() {
        return outbps;
    }

    @Bindable
    public long getOutBytesTotal() {
        return outBytesTotal;
    }

    @Bindable
    public String getAddress() {
        return address;
    }

    @Bindable
    public String getClientVersion() {
        return clientVersion;
    }

    @Bindable
    public boolean isConnected() {
        return connected;
    }

    @Bindable
    public boolean isPaused() {
        return paused;
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

    @BindingAdapter({"deviceCompletion", "deviceConnected"})
    public static void deviceCompletion(TextView view, int completion, boolean connected) {
        if (!connected || completion < 1) {
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

    public void editDevice(View btn) {
        presenter.openEditDeviceScreen(device.deviceID);
    }

    public void pauseResumeDevice(View btn) {
        if (isPaused()) {
            presenter.controller.resumeDevice(getDeviceID());
        } else {
            presenter.controller.pauseDevice(getDeviceID());
        }
    }

}
