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
 * Created by drew on 11/7/15.
 */
public class DeviceResumed extends Event<DeviceResumed.Data> {

    public DeviceResumed(long id, DateTime time, EventType type, Data data) {
        super(id, time, type, data);
    }

    public static class Data {
        public String device;
    }
}
