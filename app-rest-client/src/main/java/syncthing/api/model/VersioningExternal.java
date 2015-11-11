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
public class VersioningExternal extends Versioning<VersioningExternal.Params> {
    private static final long serialVersionUID = -2009280624991214782L;

    public VersioningExternal(VersioningType type, Params params) {
        super(type, params);
    }

    public static class Params implements Serializable, Cloneable {
        private static final long serialVersionUID = -7619269239095878091L;
        public String command;

        @Override
        protected Params clone() throws CloneNotSupportedException {
            return (Params) super.clone();
        }
    }

    @Override
    public VersioningExternal clone() {
        try {
            VersioningExternal n = (VersioningExternal) super.clone();
            n.params = params.clone();
            return n;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
