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
 * Created by drew on 3/2/15.
 */
public abstract class Event<T> {
    public final  long id;
    public final DateTime time;
    public final EventType type;
    public final T data;
    public Event(long id, DateTime time, EventType type, T data) {
        this.id = id;
        this.time = time;
        this.type = type;
        this.data = data;
    }
}
