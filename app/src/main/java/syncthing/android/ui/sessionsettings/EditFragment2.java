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

import org.opensilk.common.ui.mortarfragment.MortarFragment;

import syncthing.android.model.Credentials;

/**
 * Created by drew on 10/11/15.
 */
public abstract class EditFragment2 extends MortarFragment {

    protected Credentials mCredentials;

    protected static Bundle putCredentials(Credentials credentials) {
        Bundle b = new Bundle();
        b.putParcelable("creds", credentials);
        return b;
    }

    protected void ensureCredentials() {
        if (mCredentials == null) {
            getArguments().setClassLoader(getClass().getClassLoader());
            mCredentials = getArguments().getParcelable("creds");
        }
        if (mCredentials == null) {
            throw new NullPointerException("You forgot to supply credentials to the session");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String title = getArguments().getString("title");
        if (title != null) {
            getActivity().setTitle(title);
        }
    }
}
