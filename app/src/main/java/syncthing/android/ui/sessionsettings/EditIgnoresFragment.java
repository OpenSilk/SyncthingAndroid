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
import android.support.annotation.NonNull;

import org.opensilk.common.ui.mortar.Screen;

import syncthing.android.R;
import syncthing.android.model.Credentials;

/**
 * Created by drew on 10/11/15.
 */
public class EditIgnoresFragment extends EditFragment2 {
    public static final String NAME = EditIgnoresFragment.class.getName();

    public static Bundle makeArgs(Credentials credentials, @NonNull String folderID) {
        Bundle b = putCredentials(credentials);
        b.putString("folder", folderID);
        b.putInt("title", R.string.ignore_patterns);
        return b;
    }

    @Override
    protected Screen newScreen() {
        ensureCredentials();
        String fid = getArguments().getString("folder");
        return new EditIgnoresScreen(mCredentials, fid); //ignores
    }
}
