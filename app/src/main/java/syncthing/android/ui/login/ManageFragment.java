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

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActionBarOwner;
import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.common.ui.mortarfragment.MortarFragment;

import syncthing.android.R;
import syncthing.android.ui.LauncherActivityComponent;

/**
 * Created by drew on 3/15/15.
 */
public class ManageFragment extends MortarFragment {

    public static ManageFragment newInstance() {
        return new ManageFragment();
    }

    @Override
    protected Screen newScreen() {
        return new ManageScreen();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //TODO stop this
        ActionBarOwner actionBarOwner = DaggerService.<LauncherActivityComponent>
                getDaggerComponent(getActivity()).actionBarOwner();
        actionBarOwner.setConfig(actionBarOwner.getConfig().buildUpon().setTitle(R.string.manage_devices).build());
    }

}
