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

import java.security.cert.X509Certificate;

import syncthing.android.model.Credentials;

/**
 * Created by drew on 10/10/15.
 */
public class SyncthingApiConfig {

    final Credentials credentials;

    public SyncthingApiConfig(Credentials credentials) {
        this.credentials = credentials;
    }

    public String getBaseUrl() {
        return credentials.url;
    }

    public X509Certificate getCACert() {
        return null;
    }

    public String getApiKey() {
        return null;
    }

    public String getAuth() {
        return null;
    }
}
