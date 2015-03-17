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

package syncthing.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by drew on 3/1/15.
 */
public class OptionsConfig {
    @SerializedName("ListenAddress")public String[] listenAddress = new String[]{"0.0.0.0:22000"};
    @SerializedName("GlobalAnnServers")public String[] globalAnnounceServers = new String[]{"udp4://announce.syncthing.net:22026", "udp6://announce-v6.syncthing.net:22026"};
    @SerializedName("GlobalAnnEnabled")public boolean globalAnnounceEnabled = true;
    @SerializedName("LocalAnnEnabled")public boolean localAnnounceEnabled = true;
    @SerializedName("LocalAnnPort")public int localAnnouncePort = 21025;
    @SerializedName("LocalAnnMCAddr")public String localAnnounceMCAddr = "[ff32::5222]:21026";
    @SerializedName("MaxSendKbps")public int maxSendKbps;
    @SerializedName("MaxRecvKbps")public int maxRecvKbps;
    @SerializedName("ReconnectionIntervalS")public int reconnectionIntervalS = 60;
    @SerializedName("StartBrowser")public boolean startBrowser = true;
    @SerializedName("UPnPEnabled")public boolean upnpEnabled = true;
    @SerializedName("UPnPLease")public int upnpLeaseMinutes = 0;
    @SerializedName("UPnPRenewal")public int upnpRenewalMinutes = 30;
    @SerializedName("URAccepted")public int urAccepted; //0 off, -1 permanent
    @SerializedName("URUniqueID")public String urUniqueId;
    @SerializedName("RestartOnWakeup")public boolean restartOnWakeup = true;
    @SerializedName("AutoUpgradeIntervalH")public int autoUpgradeIntervalH = 12;
    @SerializedName("KeepTemporariesH")public int keepTemporariesH = 24;
    @SerializedName("CacheIgnoredFiles")public boolean cacheIgnoredFiles = true;
    @SerializedName("ProgressUpdateIntervalS")public int progressUpdateIntervalS = 5;
    @SerializedName("SymlinksEnabled")public boolean symlinksEnabled = true;
    @SerializedName("LimitBandwidthInLan")public boolean limitBandwidthInLan = false;
}
