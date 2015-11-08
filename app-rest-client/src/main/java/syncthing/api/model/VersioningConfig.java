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
 * Created by drew on 3/1/15.
 */
public class VersioningConfig implements Serializable, Cloneable {
    private static final long serialVersionUID = -6095511925370580788L;
    public VersioningType type = VersioningType.NONE;
    public VersioningParams params = new VersioningParams();

    @Override
    public VersioningConfig clone() {
        try {
            VersioningConfig n = (VersioningConfig) super.clone();
            n.params = params.clone();
            return n;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
