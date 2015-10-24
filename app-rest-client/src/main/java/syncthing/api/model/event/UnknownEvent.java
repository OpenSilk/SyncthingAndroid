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

/**
 * Created by drew on 10/11/15.
 */
public class UnknownEvent extends Event<String> {
    public UnknownEvent(long id, DateTime time, String json) {
        super(id, time, EventType.UNKNOWN, json);
    }
}
