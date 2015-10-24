/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model;

/**
 * Created by drew on 4/11/15.
 */
public class Connections {
    public ConnectionInfo total;
    public ConnectionInfoMap connections = ConnectionInfoMap.EMPTY;
}
