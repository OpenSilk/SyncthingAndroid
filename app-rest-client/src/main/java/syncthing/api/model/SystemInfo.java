/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Map;

import syncthing.api.ApiUtils;

/**
 * Created by drew on 3/1/15.
 */
public class SystemInfo {
    public long alloc;
    public double cpuPercent;
    public int goroutines;
    public String myID;
    public long sys;
    public String pathSeparator;
    public long uptime;
    public DateTime startTime;
    public String tilde;
    public boolean discoveryEnabled;
    public int discoveryMethods;
    public Map<String, String> discoveryErrors = Collections.emptyMap();
    public boolean relaysEnabled;
    public Map<String, Boolean> relayClientStatus = Collections.emptyMap();
    public Map<String, Integer> relayClientLatency = Collections.emptyMap();

    @Override
    public String toString() {
        return ApiUtils.reflectionToString(this);
    }
}
