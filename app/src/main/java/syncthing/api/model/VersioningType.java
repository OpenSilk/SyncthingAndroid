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

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import syncthing.android.R;

/**
* Created by drew on 3/17/15.
*/
public enum VersioningType {
    @SerializedName("simple") SIMPLE(R.string.simple_file_versioning),
    @SerializedName("staggered") STAGGERED(R.string.staggered_file_versioning),
    @SerializedName("") NONE(R.string.no);

    private int resource;

    private VersioningType(int resource) {
        this.resource = resource;
    }

    public CharSequence localizedString(Context context) {
        return context.getString(resource);
    }
}
