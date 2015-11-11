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
public class VersioningStaggered extends Versioning<VersioningStaggered.Params> {
    private static final long serialVersionUID = 2782044267939456125L;

    public VersioningStaggered(VersioningType type, Params params) {
        super(type, params);
    }

    public static class Params implements Serializable, Cloneable {
        private static final long serialVersionUID = 2150523682741493323L;
        public String versionPath;
        public String maxAge = "31536000"; //365days
        public String cleanInterval = "3600";
        @Override
        protected Params clone() throws CloneNotSupportedException {
            return (Params) super.clone();
        }
    }

    @Override
    public VersioningStaggered clone() {
        try {
            VersioningStaggered n = (VersioningStaggered) super.clone();
            n.params = params.clone();
            return n;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
