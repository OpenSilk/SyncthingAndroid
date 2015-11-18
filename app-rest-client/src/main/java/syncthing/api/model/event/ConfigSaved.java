/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model.event;

import org.joda.time.DateTime;

import syncthing.api.model.Config;

/**
 * Created by drew on 10/11/15.
 */
public class ConfigSaved extends Event<Config> {

    public ConfigSaved(long id, DateTime time, EventType type, Config data) {
        super(id, time, type, data);
    }

}
