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
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import syncthing.android.model.Credentials;
import syncthing.api.model.Config;

/**
 * Created by drew on 10/10/15.
 */
public class Session {
    public static final String EXTRA_CREDENTIALS = "creds";
    /*package*/ final AtomicInteger refs = new AtomicInteger(1);
    private final SyncthingApiConfig config;
    private final SessionComponent component;
    private final Bundle extras = new Bundle();

    public Session(SyncthingApiConfig config, SessionComponent component) {
        this.config = config;
        this.component = component;
    }

    public SessionController controller() {
        return component.controller();
    }

    public SyncthingApi api() {
        return component.syncthingApi();
    }

    SyncthingApiConfig config() {
        return config;
    }

    public Bundle extras() {
        return extras;
    }

    /*package*/ void destroy() {
        component.controller().kill();
    }
}
