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

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by drew on 3/17/15.
 */
@Module
public class EditModule {
    //cause cant return nulls
    public static final String INVALID_ID = "@@@INVALID@@@";

    final String folderId;
    final String deviceId;
    final boolean isAdd;

    public EditModule(String folderId, String deviceId, boolean isAdd) {
        this.folderId = folderId;
        this.deviceId = deviceId;
        this.isAdd = isAdd;
    }

    @Provides
    public EditPresenterConfig provideConfig() {
        EditPresenterConfig config = new EditPresenterConfig();
        config.folderId = folderId;
        config.deviceId = deviceId;
        config.isAdd = isAdd;
        return config;
    }

}
