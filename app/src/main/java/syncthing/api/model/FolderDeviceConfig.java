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

import com.google.gson.annotations.SerializedName;

/**
 * Created by drew on 3/17/15.
 */
public class FolderDeviceConfig {
    @SerializedName("DeviceID") public String deviceID;

    public FolderDeviceConfig(String deviceID) {
        this.deviceID = deviceID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderDeviceConfig that = (FolderDeviceConfig) o;

        if (deviceID != null ? !deviceID.equals(that.deviceID) : that.deviceID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return deviceID != null ? deviceID.hashCode() : 0;
    }
}
