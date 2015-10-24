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
 * Created by drew on 3/18/15.
 */
public enum Compression {
    @SerializedName("always") ALWAYS,
    @SerializedName("metadata") METADATA,
    @SerializedName("never") NEVER,
    UNKNOWN,
}
