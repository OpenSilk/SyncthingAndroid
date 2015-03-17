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

package syncthing.android.ui.session.edit;

import org.opensilk.common.mortar.WithComponent;
import org.opensilk.common.mortarfragment.Layout;

import syncthing.android.R;

import static syncthing.android.ui.session.edit.EditModule.INVALID_ID;

/**
 * Created by drew on 3/16/15.
 */
@Layout(R.layout.screen_edit_folder)
@WithComponent(EditFolderComponent.class)
public class EditFolderScreen {

    final String folderId;
    final String deviceId;
    final boolean isAdd;

    public EditFolderScreen() {
        this(INVALID_ID, INVALID_ID, true);
    }

    public EditFolderScreen(String folderId) {
        this(folderId, INVALID_ID, false);
    }

    public EditFolderScreen(String folderId, String deviceId) {
        this(folderId, deviceId, false);
    }

    public EditFolderScreen(String folderId, String deviceId, boolean isAdd) {
        this.folderId = folderId;
        this.deviceId = deviceId;
        this.isAdd = isAdd;
    }
}
