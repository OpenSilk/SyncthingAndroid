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
import java.util.Arrays;

/**
 * Created by drew on 3/1/15.
 */
public class OptionsConfig implements Serializable, Cloneable {
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
    public boolean urPostInsecurely;
    public int urInitialDelayS;
    public String urURL;
    public boolean restartOnWakeup;
    public int autoUpgradeIntervalH;
    public int keepTemporariesH;
    public boolean cacheIgnoredFiles;
    public int progressUpdateIntervalS;
    public boolean symlinksEnabled;
    public boolean limitBandwidthInLan;
    public int databaseBlockCacheMiB;
    public String releasesURL;
    public String[] alwaysLocalNets;
    public boolean relayWithoutGlobalAnn;
    public String[] relayServers;
    public int minHomeDiskFreePct;

    public static OptionsConfig withDefaults() {
        OptionsConfig o = new OptionsConfig();
        o.listenAddress = new String[]{"tcp://0.0.0.0:22000"};
        o.globalAnnounceServers = new String[]{"default"};
        o.globalAnnounceEnabled = true;
        o.localAnnounceEnabled = true;
        o.localAnnouncePort = 21027;
        o.localAnnounceMCAddr = "[ff12::8384]:21027";
        o.maxSendKbps = 0;
        o.maxRecvKbps = 0;
        o.reconnectionIntervalS = 60;
        o.startBrowser = true;
        o.upnpEnabled = true;
        o.upnpLeaseMinutes = 60;
        o.upnpRenewalMinutes = 30;
        o.upnpTimeoutSeconds = 10;
        o.urAccepted = -1; //0 off, -1 permanent
        o.urPostInsecurely = false;
        o.urInitialDelayS = 1800;
        o.urURL = "https://data.syncthing.net/newdata";
        o.restartOnWakeup = true;
        o.autoUpgradeIntervalH = 12;
        o.keepTemporariesH = 24;
        o.cacheIgnoredFiles = true;
        o.progressUpdateIntervalS = 5;
        o.symlinksEnabled = true;
        o.limitBandwidthInLan = false;
        o.databaseBlockCacheMiB = 0;
        o.releasesURL = "https://api.github.com/repos/syncthing/syncthing/releases?per_page=30";
        o.relayWithoutGlobalAnn = false;
        o.relayServers = new String[]{"dynamic+https://relays.syncthing.net/endpoint"};
        o.minHomeDiskFreePct = 1;
        return o;
    }

    @Override
    public OptionsConfig clone() {
        try {
            OptionsConfig n = (OptionsConfig) super.clone();
            if (listenAddress != null && listenAddress.length > 0) {
                n.listenAddress = Arrays.copyOf(listenAddress, listenAddress.length);
            }
            if (globalAnnounceServers != null && globalAnnounceServers.length > 0) {
                n.globalAnnounceServers = Arrays.copyOf(globalAnnounceServers, globalAnnounceServers.length);
            }
            if (alwaysLocalNets != null && alwaysLocalNets.length > 0) {
                n.alwaysLocalNets = Arrays.copyOf(alwaysLocalNets, alwaysLocalNets.length);
            }
            if (relayServers != null && relayServers.length > 0) {
                n.relayServers = Arrays.copyOf(relayServers, relayServers.length);
            }
            return n;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
