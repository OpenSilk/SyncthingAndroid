/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package syncthing.api;

import android.os.Bundle;

import java.util.concurrent.atomic.AtomicInteger;

import syncthing.android.model.Credentials;

/**
 * Created by drew on 10/10/15.
 */
public class Session {
    /*package*/ final AtomicInteger refs = new AtomicInteger(1);
    private final Credentials credentials;
    private final SessionComponent component;
    private final Bundle extras = new Bundle();

    public Session(Credentials credentials, SessionComponent component) {
        this.credentials = credentials;
        this.component = component;
    }

    public SessionController controller() {
        return component.controller();
    }

    public Credentials credentials() {
        return credentials;
    }

    public SyncthingApi api() {
        return component.syncthingApi();
    }

    /*package*/ void destroy() {
        component.controller().kill();
    }
}
