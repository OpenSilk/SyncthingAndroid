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

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.opensilk.common.ui.mortar.Screen;

import syncthing.android.R;
import syncthing.api.Credentials;

/**
 * Created by drew on 10/11/15.
 */
public class EditFolderFragment extends EditFragment2 {
    public static final String NAME = EditFolderFragment.class.getName();

    public static Bundle makeArgs(Credentials credentials, @Nullable String folder, @Nullable String device) {
        Bundle b = putCredentials(credentials);
        b.putString("folder", folder);
        b.putString("device", device);
        if (folder != null && device != null) {
            b.putInt("title", R.string.share_folder);
        } else if (folder != null) {
            b.putInt("title", R.string.edit_folder);
        } else {
            b.putInt("title", R.string.add_folder);
        }
        return b;
    }

    @Override
    protected Screen newScreen() {
        ensureCredentials();
        String fid = getArguments().getString("folder");
        String did = getArguments().getString("device");
        if (fid != null && did != null) {
            return new EditFolderScreen(mCredentials, fid, did);//share
        } else if (fid != null) {
            return new EditFolderScreen(mCredentials, fid);//Edit
        } else {
            return new EditFolderScreen(mCredentials);//Add
        }
    }
}
