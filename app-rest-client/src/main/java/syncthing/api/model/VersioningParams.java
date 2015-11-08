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
 * TODO separate into own classes
 * Created by drew on 3/17/15.
 */
public class VersioningParams implements Serializable, Cloneable {
    private static final long serialVersionUID = 6669162098435872133L;
    //TrashCan
    public String cleanoutDays = "0";
    //Simple
    public String keep = "5";
    //Staggered
    public String versionPath;
    public String maxAge = "31536000"; //365days
    public String cleanInterval = "3600";
    //External
    public String command;

    @Override
    public VersioningParams clone() {
        try {
            return (VersioningParams) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
