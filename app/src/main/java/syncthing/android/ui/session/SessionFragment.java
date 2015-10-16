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

package syncthing.android.ui.session;

import android.os.Bundle;

import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.common.ui.mortarfragment.MortarFragment;

import syncthing.android.model.Credentials;

/**
 * Created by drew on 3/11/15.
 */
public class SessionFragment extends MortarFragment {

    public static SessionFragment newInstance(Credentials credentials) {
        SessionFragment f = new SessionFragment();
        Bundle b = new Bundle(1);
        b.putParcelable("creds", credentials);
        f.setArguments(b);
        return f;
    }

    Credentials mCredentials;

    @Override
    protected Screen newScreen() {
        ensureCredentials();
        return new SessionScreen(mCredentials);
    }

    void ensureCredentials() {
        if (mCredentials == null) {
            getArguments().setClassLoader(getClass().getClassLoader());
            mCredentials = getArguments().getParcelable("creds");
        }
        if (mCredentials == null) {
            throw new NullPointerException("You forgot to supply credentials to the session");
        }
    }

}
