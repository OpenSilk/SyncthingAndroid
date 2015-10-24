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
public class VersioningParams implements Serializable {
    private static final long serialVersionUID = 3403767982430372571L;
    //Simple
    public String keep = "5";
    //Staggered
    public String versionPath;
    public String maxAge = "31536000"; //365days
    public String cleanInterval = "3600";
    //External
    public String command;
}
