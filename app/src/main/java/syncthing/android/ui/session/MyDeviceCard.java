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
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.DecimalFormat;

import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ExpandableCard;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;

/**
 * Created by drew on 3/10/15.
 */
public class MyDeviceCard extends ExpandableCard {

    static final DecimalFormat cpuFormat = new DecimalFormat("0.00");
    static final PeriodFormatter uptimeFormatter;
    static {
        uptimeFormatter = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix("d ")
                .appendHours()
                .appendSuffix("h ")
                .appendMinutes()
                .appendSuffix("m ")
                .appendSeconds()
                .appendSuffix("s")
                .toFormatter();
    }

    protected DeviceConfig device;
    protected ConnectionInfo connection;
    protected SystemInfo system;
    protected Version version;

    public MyDeviceCard(DeviceConfig device, ConnectionInfo connection, SystemInfo system, Version version) {
        this.device = device;
        this.connection = connection;
        this.system = system;
        this.version = version;
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
        } else {
            ConnectionInfo oldConnection = this.connection;
            this.connection = connection;
            if (oldConnection.inbps != connection.inbps) {
                notifyChange(syncthing.android.BR.inbps);
            }
            if (oldConnection.inBytesTotal != connection.inBytesTotal) {
                notifyChange(syncthing.android.BR.inBytesTotal);
            }
            if (oldConnection.outbps != connection.outbps) {
                notifyChange(syncthing.android.BR.outbps);
            }
            if (oldConnection.outBytesTotal != connection.outBytesTotal) {
                notifyChange(syncthing.android.BR.outBytesTotal);
            }
        }
    }

    public void setSystemInfo(SystemInfo system) {
        if (this.system == null) {
            this.system = system;
            notifyChange(syncthing.android.BR.mem);
            notifyChange(syncthing.android.BR.cpuPercent);
            notifyChange(syncthing.android.BR.cpuPercentText);
            notifyChange(syncthing.android.BR.systemInfo);
            notifyChange(syncthing.android.BR.hasSystemInfo);
            notifyChange(syncthing.android.BR.showGlobalAnnounce);
            notifyChange(syncthing.android.BR.uptime);
            notifyChange(syncthing.android.BR.uptimeText);
        } else {
            SystemInfo oldSystem = this.system;
            this.system = system;
            if (oldSystem.sys != system.sys) {
                notifyChange(syncthing.android.BR.mem);
            }
            if (oldSystem.cpuPercent != system.cpuPercent) {
                notifyChange(syncthing.android.BR.cpuPercent);
                notifyChange(syncthing.android.BR.cpuPercentText);
            }
            if (oldSystem.uptime != system.uptime) {
                notifyChange(syncthing.android.BR.uptime);
            }
            //todo handle global announce better
            if (oldSystem.announceServersFailed.size()
                    != system.announceServersFailed.size()) {
                notifyChange(syncthing.android.BR.systemInfo);
                notifyChange(syncthing.android.BR.showGlobalAnnounce);
            }
        }
    }

    public void setVersion(Version version) {
        this.version = version;
        notifyChange(syncthing.android.BR.versionText);
    }

    @Override
    public int getLayout() {
        return R.layout.session_mydevice;
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
    public long getMem() {
        return system != null ? system.sys : -1;
    }

    @Bindable
    public double getCpuPercent() {
        return system != null ? system.cpuPercent : -1.0;
    }

    @Bindable
    public String getCpuPercentText() {
        return cpuFormat.format(getCpuPercent());
    }

    @Bindable
    public SystemInfo getSystemInfo() {
        return system;
    }

    @Bindable
    public boolean getHasSystemInfo() {
        return system != null;
    }

    @Bindable
    public boolean getShowGlobalAnnounce() {
        return system != null && system.extAnnounceOK != null && system.announceServersTotal > 0;
    }

    @Bindable
    public long getUptime() {
        return system != null ? system.uptime : -1;
    }

    @Bindable
    public String getUptimeText() {
        return uptimeFormatter.print(Duration.standardSeconds(getUptime()).toPeriod());
    }

    @Bindable
    public String getVersionText() {
        return version != null ? version.toString() : "?";
    }

    @BindingAdapter("globalAnnounce")
    public static void setGlobalAnnounce(TextView view, SystemInfo sys) {
        if (sys != null && sys.extAnnounceOK != null && sys.announceServersTotal > 0) {
            if (sys.announceServersFailed.isEmpty()) {
                view.setText(android.R.string.ok);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.announce_ok));
            } else {
                int successes = (sys.announceServersTotal - sys.announceServersFailed.size());
                view.setText(view.getResources().getString(R.string.announce_failures,
                        successes, sys.announceServersTotal
                ));
                view.setTextColor(ContextCompat.getColor(view.getContext(),
                                successes == 0
                                ? R.color.announce_fail
                                : R.color.announce_ok
                ));
            }
        }//else its hidden
    }
}
