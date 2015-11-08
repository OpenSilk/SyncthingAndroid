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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by drew on 3/1/15.
 */
public class FolderConfig implements Serializable, Cloneable {
    private static final long serialVersionUID = -1620434875319958462L;
    public String id;
    public String path;
    public List<FolderDeviceConfig> devices = Collections.emptyList();
    public boolean readOnly;
    public int rescanIntervalS;
    public boolean ignorePerms;
    public boolean autoNormalize;
    public VersioningConfig versioning;
    public int copiers;// = 1;
    public int pullers;// = 16;
    public int hashers;// = 0;
    public PullOrder order = PullOrder.UNKNOWN;
    public String invalid;

    public static FolderConfig withDefaults() {
        FolderConfig f = new FolderConfig();
        f.devices = new ArrayList<>();
        f.readOnly = false;
        f.rescanIntervalS = 86400;
        f.autoNormalize = true;
        f.versioning = new VersioningConfig();
        f.order = PullOrder.RANDOM;
        f.invalid = "";
        return f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderConfig folder = (FolderConfig) o;

        if (id != null ? !id.equals(folder.id) : folder.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public FolderConfig clone() {
        try {
            FolderConfig n = (FolderConfig) super.clone();
            if (devices != null && !devices.isEmpty()) {
                n.devices = new ArrayList<>(devices);
            }
            if (versioning != null) {
                n.versioning = versioning.clone();
            }
            return n;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
