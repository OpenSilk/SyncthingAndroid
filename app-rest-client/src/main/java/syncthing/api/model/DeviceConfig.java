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
import java.util.Arrays;

import syncthing.api.ApiUtils;

/**
 * Created by drew on 3/1/15.
 */
public class DeviceConfig implements Serializable, Cloneable {
    private static final long serialVersionUID = 2383227051854131929L;
    public String deviceID;
    public String name;
    public String[] addresses;
    public Compression compression;
    public String certName;
    public boolean introducer;

    public static DeviceConfig withDefaults() {
        DeviceConfig d = new DeviceConfig();
        d.addresses = new String[]{"dynamic"};
        d.compression = Compression.METADATA;
        d.introducer = false;
        return d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceConfig device = (DeviceConfig) o;

        if (deviceID != null ? !deviceID.equals(device.deviceID) : device.deviceID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return deviceID != null ? deviceID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return ApiUtils.reflectionToString(this);
    }

    @Override
    public DeviceConfig clone() {
        try {
            DeviceConfig n = (DeviceConfig)super.clone();
            if (addresses != null) {
                addresses = Arrays.copyOf(addresses, addresses.length);
            }
            return n;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
