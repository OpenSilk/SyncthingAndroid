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
    protected Version version;

    //connection info;
    private long inbps = -1;
    private long inBytesTotal = -1;
    private long outbps = -1;
    private long outBytesTotal = -1;

    //system info
    private boolean hasSystemInfo;
    private long sys;
    private double cpuPercent;
    private long uptime;
    private boolean discoveryEnabled;
    private int discoveryFailures;
    private int discoveryMethods;
    private boolean relaysEnabled;
    private int relaysFailures;
    private int relaysTotal;

    public MyDeviceCard(DeviceConfig device, ConnectionInfo connection, SystemInfo system, Version version) {
        this.device = device;
        this.version = version;
        setConnectionInfo(connection);
        setSystemInfo(system);
        setExpanded(true);
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
        if (connection == null) {
            inbps = inBytesTotal = outbps = outBytesTotal = -1;
            notifyChange(syncthing.android.BR.inbps);
            notifyChange(syncthing.android.BR.inBytesTotal);
            notifyChange(syncthing.android.BR.outbps);
            notifyChange(syncthing.android.BR.outBytesTotal);
        } else {
            if (inbps != connection.inbps || inBytesTotal != connection.inBytesTotal) {
                inbps = connection.inbps;
                inBytesTotal = connection.inBytesTotal;
                notifyChange(syncthing.android.BR.inbps);
                notifyChange(syncthing.android.BR.inBytesTotal);
            }
            if (outbps != connection.outbps || outBytesTotal != connection.outBytesTotal) {
                outbps = connection.outbps;
                outBytesTotal = connection.outBytesTotal;
                notifyChange(syncthing.android.BR.outbps);
                notifyChange(syncthing.android.BR.outBytesTotal);
            }
        }
    }

    public void setSystemInfo(SystemInfo system) {
        if (system == null) {
            hasSystemInfo = false;
            discoveryEnabled = relaysEnabled = false;
            notifyChange(syncthing.android.BR.hasSystemInfo);
            notifyChange(syncthing.android.BR.mem);
            notifyChange(syncthing.android.BR.cpuPercent);
            notifyChange(syncthing.android.BR.cpuPercentText);
            notifyChange(syncthing.android.BR.uptime);
            notifyChange(syncthing.android.BR.uptimeText);
            notifyChange(syncthing.android.BR.discoveryEnabled);
            notifyChange(syncthing.android.BR.discoveryFailures);
            notifyChange(syncthing.android.BR.discoveryMethods);
            notifyChange(syncthing.android.BR.relaysEnabled);
            notifyChange(syncthing.android.BR.relaysFailures);
            notifyChange(syncthing.android.BR.relaysTotal);
        } else {
            if (!hasSystemInfo) {
                hasSystemInfo = true;
                notifyChange(syncthing.android.BR.hasSystemInfo);
            }
            if (sys != system.sys) {
                sys = system.sys;
                notifyChange(syncthing.android.BR.mem);
            }
            if (cpuPercent != system.cpuPercent) {
                cpuPercent = system.cpuPercent;
                notifyChange(syncthing.android.BR.cpuPercent);
                notifyChange(syncthing.android.BR.cpuPercentText);
            }
            if (uptime != system.uptime) {
                uptime = system.uptime;
                notifyChange(syncthing.android.BR.uptime);
                notifyChange(syncthing.android.BR.uptimeText);
            }
            if (discoveryEnabled != system.discoveryEnabled) {
                discoveryEnabled = system.discoveryEnabled;
                notifyChange(syncthing.android.BR.discoveryEnabled);
            }
            int failures = 0;
            if (system.discoveryErrors != null && !system.discoveryErrors.isEmpty()) {
                failures = system.discoveryErrors.size();
            }
            if (discoveryFailures != failures || discoveryMethods != system.discoveryMethods) {
                discoveryFailures = failures;
                discoveryMethods = system.discoveryMethods;
                notifyChange(syncthing.android.BR.discoveryFailures);
                notifyChange(syncthing.android.BR.discoveryMethods);
            }
            if (relaysEnabled != system.relaysEnabled) {
                relaysEnabled = system.relaysEnabled;
                notifyChange(syncthing.android.BR.relaysEnabled);
            }
            int failed = 0, total = 0;
            if (system.relayClientStatus != null) {
                for (String r : system.relayClientStatus.keySet()) {
                    if (!system.relayClientStatus.get(r)) {
                        failed++;
                    }
                    total++;
                }
            }
            if (relaysFailures != failed || relaysTotal != total) {
                relaysFailures = failed;
                relaysTotal = total;
                notifyChange(syncthing.android.BR.relaysFailures);
                notifyChange(syncthing.android.BR.relaysTotal);
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
    public long getMem() {
        return sys;
    }

    @Bindable
    public double getCpuPercent() {
        return cpuPercent;
    }

    @Bindable
    public String getCpuPercentText() {
        return cpuFormat.format(getCpuPercent());
    }

    @Bindable
    public boolean getHasSystemInfo() {
        return hasSystemInfo;
    }

    @Bindable
    public long getUptime() {
        return uptime;
    }

    @Bindable
    public String getUptimeText() {
        return uptimeFormatter.print(Duration.standardSeconds(getUptime()).toPeriod());
    }

    @Bindable
    public String getVersionText() {
        return version != null ? version.toString() : "?";
    }

    @Bindable
    public boolean isDiscoveryEnabled() {
        return discoveryEnabled;
    }

    @Bindable
    public int getDiscoveryFailures() {
        return discoveryFailures;
    }

    @Bindable
    public int getDiscoveryMethods() {
        return discoveryMethods;
    }

    @Bindable
    public boolean isRelaysEnabled() {
        return relaysEnabled;
    }

    @Bindable
    public int getRelaysFailures() {
        return relaysFailures;
    }

    @Bindable
    public int getRelaysTotal() {
        return relaysTotal;
    }

}
