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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.opensilk.common.mortarfragment.MortarFragment;
import org.opensilk.common.mortarfragment.MortarFragmentUtils;

import syncthing.android.R;
import syncthing.android.model.Credentials;

/**
 * Created by drew on 3/11/15.
 */
public class SessionFragment extends MortarFragment implements SessionFragmentPresenter.Fragment {

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
    SessionFragmentPresenter mFragmentPresenter;

    @Override
    protected Object getScreen() {
        ensureCredentials();
        return new SessionScreen(mCredentials);
    }

    @Override
    protected String getScopeName() {
        ensureCredentials();
        return super.getScopeName() + "-" + mCredentials.apiKey;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = MortarFragmentUtils.<SessionComponent>getDaggerComponent(mScope).presenter();
        mFragmentPresenter = MortarFragmentUtils.<SessionComponent>getDaggerComponent(mScope).fragmentPresenter();
        mFragmentPresenter.takeView(this);
    }

    @Override
    public void onDestroy() {
        mFragmentPresenter.dropView(this);
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(mCredentials.alias);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.session, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device:
                mPresenter.openAddDeviceScreen();
                return true;
            case R.id.add_folder:
                mPresenter.openAddFolderScreen();
                return true;
            case R.id.settings:
                return true;
            case R.id.show_id: {
                Context context = mScope.createContext(getActivity());
                View v = LayoutInflater.from(context).inflate(R.layout.dialog_show_id, null);
                new AlertDialog.Builder(context)
                        .setTitle(R.string.device_id)
                        .setView(v)
                        .setPositiveButton(R.string.close, null)
                        .show();
                return true;
            } case R.id.shutdown: {
                Context context = mScope.createContext(getActivity());
                new AlertDialog.Builder(context)
                        .setTitle(R.string.shutdown)
                        .setMessage(R.string.are_you_sure_you_want_to_shutdown_syncthing)
                        .setPositiveButton(R.string.shutdown, (dialog, which) -> mPresenter.controller.shutdown())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            } case R.id.restart:
                mPresenter.controller.restart();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void ensureCredentials() {
        if (mCredentials == null) {
            mCredentials = getArguments().getParcelable("creds");
        }
        if (mCredentials == null) {
            throw new NullPointerException("You forgot to supply credentils to the session");
        }
    }

}
