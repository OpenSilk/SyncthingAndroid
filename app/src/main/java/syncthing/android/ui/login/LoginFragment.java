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

package syncthing.android.ui.login;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.common.ui.mortarfragment.MortarFragment;

import syncthing.api.Credentials;

/**
 * Created by drew on 3/10/15.
 */
public class LoginFragment extends MortarFragment {
    public static final String NAME = LoginFragment.class.getName();

    public static LoginFragment newInstance() {
        return newInstance(Credentials.NONE);
    }

    public static LoginFragment newInstance(@NonNull Credentials credentials) {
        LoginFragment f = new LoginFragment();
        Bundle b = new Bundle();
        b.putParcelable("creds", credentials);
        f.setArguments(b);
        return f;
    }

    @Override
    protected Screen newScreen() {
        ensureCredentials();
        return new LoginScreen(mCredentials);
    }

    Credentials mCredentials;

    void ensureCredentials() {
        if (getArguments() != null) {
            getArguments().setClassLoader(getClass().getClassLoader());
            mCredentials = getArguments().getParcelable("creds");
        }
        if (mCredentials == null) {
            mCredentials = Credentials.NONE;
        }
    }
}
