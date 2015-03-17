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

/**
 * Created by drew on 3/1/15.
 */
public class OptionsConfig {
    public String[] listenAddress = new String[]{"0.0.0.0:22000"};
    public String[] globalAnnounceServers = new String[]{"udp4://announce.syncthing.net:22026", "udp6://announce-v6.syncthing.net:22026"};
    public boolean globalAnnounceEnabled = true;
    public boolean localAnnounceEnabled = true;
    public int localAnnouncePort = 21025;
    public String localAnnounceMCAddr = "[ff32::5222]:21026";
    public int maxSendKbps;
    public int maxRecvKbps;
    public int reconnectionIntervalS = 60;
    public boolean startBrowser = true;
    public boolean upnpEnabled = true;
    public int upnpLeaseMinutes = 0;
    public int upnpRenewalMinutes = 30;
    public int urAccepted; //0 off, -1 permanent
    public String urUniqueId;
    public boolean restartOnWakeup = true;
    public int autoUpgradeIntervalH = 12;
    public int keepTemporariesH = 24;
    public boolean cacheIgnoredFiles = true;
    public int progressUpdateIntervalS = 5;
    public boolean symlinksEnabled = true;
    public boolean limitBandwidthInLan = false;
}
