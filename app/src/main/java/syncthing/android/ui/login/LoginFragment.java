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
import android.support.annotation.Nullable;

import org.opensilk.common.mortarfragment.MortarFragment;

import syncthing.android.R;
import syncthing.android.model.Credentials;

/**
 * Created by drew on 3/10/15.
 */
public class LoginFragment extends MortarFragment {

    public static LoginFragment newInstance(Credentials credentials) {
        LoginFragment f = new LoginFragment();
        Bundle b = new Bundle();
        b.putParcelable(LoginActivity.EXTRA_CREDENTIALS, credentials);
        f.setArguments(b);
        return f;
    }

    @Override
    protected Object getScreen() {
        ensureCredentials();
        return new LoginScreen(mCredentials);
    }

    Credentials mCredentials;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.login);
    }

    void ensureCredentials() {
        mCredentials = getArguments().getParcelable(LoginActivity.EXTRA_CREDENTIALS);
        if (mCredentials == null) {
            mCredentials = Credentials.NONE;
        }
    }
}
