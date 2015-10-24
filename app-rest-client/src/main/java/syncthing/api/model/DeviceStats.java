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
 * Created by drew on 3/4/15.
 */
public class DeviceStats {
    public DateTime lastSeen;

    @Override
    public String toString() {
        return ApiUtils.reflectionToString(this);
    }
}
