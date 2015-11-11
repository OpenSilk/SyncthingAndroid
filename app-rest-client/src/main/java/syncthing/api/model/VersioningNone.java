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
public class VersioningNone extends Versioning<VersioningNone.Params> {
    private static final long serialVersionUID = -3409061834421610335L;

    public VersioningNone(VersioningType type) {
        super(type, Params.INSTANCE);
    }

    public static class Params implements Serializable, Cloneable {
        private static final long serialVersionUID = 7458696063620732833L;
        private static final Params INSTANCE = new Params();
    }

    @Override
    public VersioningNone clone() {
        return (VersioningNone) super.clone();
    }
}
