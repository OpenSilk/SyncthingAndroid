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
 * Used by ItemStarted/ItemFinished
 * Created by drew on 10/11/15.
 */
public enum ItemAction {
    @SerializedName("update") UPDATE,
    @SerializedName("metadata") METADATA,
    @SerializedName("delete") DELETE
}
