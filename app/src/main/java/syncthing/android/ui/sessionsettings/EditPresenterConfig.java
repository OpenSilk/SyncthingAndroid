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

package syncthing.android.ui.sessionsettings;

import org.opensilk.common.core.util.Preconditions;

import syncthing.api.Credentials;

/**
 * Created by drew on 10/10/15.
 */
public class EditPresenterConfig {
    public static final String INVALID_ID = "@@@INVALID@@@";
    public final String folderId;
    public final String deviceId;
    public final boolean isAdd;
    public final Credentials credentials;

    private EditPresenterConfig(Builder builder) {
        this.folderId = builder.folderId;
        this.deviceId = builder.deviceId;
        this.isAdd = builder.isAdd;
        this.credentials = Preconditions.checkNotNull(builder.credentials, "Must set credentials");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String folderId;
        private String deviceId;
        private boolean isAdd = false;
        private Credentials credentials;

        public Builder setFolderId(String folderId) {
            this.folderId = folderId;
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder setIsAdd(boolean isAdd) {
            this.isAdd = isAdd;
            return this;
        }

        public Builder setCredentials(Credentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public EditPresenterConfig build() {
            if (folderId == null) folderId = INVALID_ID;
            if (deviceId == null) deviceId = INVALID_ID;
            return new EditPresenterConfig(this);
        }
    }
}
