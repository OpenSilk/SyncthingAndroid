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
 * Created by drew on 11/10/15.
 */
public abstract class Versioning<T> implements Serializable, Cloneable {
    private static final long serialVersionUID = -317033636068012348L;
    public VersioningType type;
    public T params;
    protected Versioning(VersioningType type, T params) {
        this.type = type;
        this.params = params;
    }
    public Versioning clone() {
        try {
            return (Versioning) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
