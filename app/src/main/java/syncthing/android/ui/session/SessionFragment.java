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

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.common.ui.mortar.ToolbarOwner;
import org.opensilk.common.ui.mortarfragment.MortarFragment;

import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.android.ui.LauncherActivity;
import syncthing.android.ui.LauncherActivityComponent;

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
    //Holding this reference is a hack to hook lifecycle
    //TODO add lifecycle hooks to fragmentPresenter and move actionbar stuff to SessionPresenter
    SessionPresenter mPresenter;

    @Override
    protected Screen newScreen() {
        ensureCredentials();
        return new SessionScreen(mCredentials);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = DaggerService.<SessionComponent>getDaggerComponent(getScope()).presenter();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.controller.init();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.controller.suspend();
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
