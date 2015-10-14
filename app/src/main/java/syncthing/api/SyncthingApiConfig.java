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

    final String url;
    final String caCert;
    final String apiKey;
    final String auth;

    private SyncthingApiConfig(Builder builder) {
        this.url = builder.url;
        this.caCert = builder.caCert;
        this.apiKey = builder.apiKey;
        this.auth = builder.auth;
    }

    public String getBaseUrl() {
        return url;
    }

    public X509Certificate getCACert() {
        return SyncthingSSLSocketFactory.makeCert(caCert);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAuth() {
        return auth;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        String url;
        String caCert;
        String apiKey;
        String auth;

        public Builder forCredentials(Credentials credentials) {
            this.url = credentials.url;
            this.caCert = credentials.caCert;
            this.apiKey = credentials.apiKey;
            this.auth = null;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setCaCert(String caCert) {
            this.caCert = caCert;
            return this;
        }

        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder setAuth(String auth) {
            this.auth = auth;
            return this;
        }

        public SyncthingApiConfig build() {
            return new SyncthingApiConfig(this);
        }
    }
}
