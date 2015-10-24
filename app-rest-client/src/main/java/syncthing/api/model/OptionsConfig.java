/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model;

import java.io.Serializable;

/**
 * Created by drew on 3/1/15.
 */
public class OptionsConfig implements Serializable {
    private static final long serialVersionUID = -6584204552575433781L;
    public String[] listenAddress;
    public String[] globalAnnounceServers;
    public boolean globalAnnounceEnabled;
    public boolean localAnnounceEnabled;
    public int localAnnouncePort;
    public String localAnnounceMCAddr;
    public int maxSendKbps;
    public int maxRecvKbps;
    public int reconnectionIntervalS;
    public boolean startBrowser;
    public boolean upnpEnabled;
    public int upnpLeaseMinutes;
    public int upnpRenewalMinutes;
    public int upnpTimeoutSeconds;
    public int urAccepted;
    public String urUniqueId;
    public boolean restartOnWakeup;
    public int autoUpgradeIntervalH;
    public int keepTemporariesH;
    public boolean cacheIgnoredFiles;
    public int progressUpdateIntervalS;
    public boolean symlinksEnabled;
    public boolean limitBandwidthInLan;
    public int databaseBlockCacheMiB;

    public static OptionsConfig withDefaults() {
        OptionsConfig o = new OptionsConfig();
        o.listenAddress = new String[]{"0.0.0.0:22000"};
        o.globalAnnounceServers = new String[]{"udp4://announce.syncthing.net:22026", "udp6://announce-v6.syncthing.net:22026"};
        o.globalAnnounceEnabled = true;
        o.localAnnounceEnabled = true;
        o.localAnnouncePort = 21025;
        o.localAnnounceMCAddr = "[ff32::5222]:21026";
        o.maxSendKbps = 0;
        o.maxRecvKbps = 0;
        o.reconnectionIntervalS = 60;
        o.startBrowser = true;
        o.upnpEnabled = true;
        o.upnpLeaseMinutes = 60;
        o.upnpRenewalMinutes = 30;
        o.upnpTimeoutSeconds = 10;
        o.urAccepted = -1; //0 off, -1 permanent
        o.restartOnWakeup = true;
        o.autoUpgradeIntervalH = 12;
        o.keepTemporariesH = 24;
        o.cacheIgnoredFiles = true;
        o.progressUpdateIntervalS = 5;
        o.symlinksEnabled = true;
        o.limitBandwidthInLan = false;
        o.databaseBlockCacheMiB = 0;
        return o;
    }
}
