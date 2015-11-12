/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by drew on 11/6/15.
 */
public enum ConnectionType {
    @SerializedName("direct-accept") DIRECT_ACCEPT,
    @SerializedName("direct-dial") DIRECT_DIAL,
    @SerializedName("relay-accept") RELAY_ACCEPT,
    @SerializedName("relay-dial") RELAY_DIAL,
    UNKNOWN
}
