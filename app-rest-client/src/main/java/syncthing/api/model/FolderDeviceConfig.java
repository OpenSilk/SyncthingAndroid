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

/**
 * Created by drew on 3/17/15.
 */
public class FolderDeviceConfig implements Serializable {
    private static final long serialVersionUID = 6021159249890537441L;
    public String deviceID;

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
