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
 * Created by drew on 4/25/15.
 */
public enum PullOrder {
    @SerializedName("random") RANDOM,
    @SerializedName("alphabetic") ALPHABETIC,
    @SerializedName("smallestFirst") SMALLESTFIRST,
    @SerializedName("largestFirst") LARGESTFIRST,
    @SerializedName("oldestFirst") OLDESTFIRST,
    @SerializedName("newestFirst") NEWESTFIRST,
    @SerializedName("unknown") UNKNOWN,
}
