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

import syncthing.api.ApiUtils;

/**
 * Created by drew on 3/1/15.
 */
public class ConnectionInfo {
    public DateTime at;
    public long inBytesTotal;
    public long outBytesTotal;
    public String address;
    public String clientVersion;
    public boolean paused;
    public boolean connected;
    public ConnectionType type = ConnectionType.UNKNOWN;
    //pojo only
    public transient String deviceId;
    public transient long inbps;
    public transient long outbps;
    public transient long lastUpdate;

    @Override
    public String toString() {
        return ApiUtils.reflectionToString(this);
    }
}
