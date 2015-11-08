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

package syncthing.android.ui.folderpicker;

import android.content.Context;
import android.os.Bundle;

import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.common.ui.mortarfragment.MortarFragment;

import syncthing.api.Credentials;

/**
 * Created by drew on 11/3/15.
 */
public class FolderPickerFragment extends MortarFragment {
    public static final String NAME = FolderPickerFragment.class.getName();

    public static Bundle makeArgs(Credentials credentials, String path) {
        Bundle b = new Bundle();
        b.putParcelable("creds", credentials);
        b.putString("path", path);
        return b;
    }

    public static FolderPickerFragment ni(Context context, Credentials credentials, String path) {
        return factory(context, NAME, makeArgs(credentials, path));
    }

    @Override
    protected Screen newScreen() {
        Credentials credentials = getArguments().getParcelable("creds");
        String path = getArguments().getString("path");
        return new FolderPickerScreen(credentials, path);
    }
}
