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

import android.app.Activity;
import android.os.Bundle;

import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortarfragment.FragmentManagerOwner;

import java.util.List;

import javax.inject.Inject;

import mortar.ViewPresenter;
import syncthing.android.AppSettings;
import syncthing.android.identicon.IdenticonGenerator;
import syncthing.android.model.Credentials;

/**
 * Created by drew on 3/15/15.
 */
@ManageScreenScope
public class ManagePresenter extends ViewPresenter<ManageScreenView> {

    final IdenticonGenerator identiconGenerator;
    final FragmentManagerOwner fragmentManagerOwner;
    final AppSettings appSettings;
    final ActivityResultsController activityResultsController;

    @Inject
    public ManagePresenter(
            IdenticonGenerator identiconGenerator,
            FragmentManagerOwner fragmentManagerOwner,
            AppSettings appSettings,
            ActivityResultsController activityResultsController
    ) {
        this.identiconGenerator = identiconGenerator;
        this.fragmentManagerOwner = fragmentManagerOwner;
        this.appSettings = appSettings;
        this.activityResultsController = activityResultsController;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        reload();
    }

    void reload() {
        List<Credentials> creds = appSettings.getSavedCredentialsSorted();
        Credentials def = appSettings.getDefaultCredentials();
        getView().load(creds, def);
    }

    void openAddScreen() {
        LoginFragment f = LoginFragment.newInstance(null);
        fragmentManagerOwner.replaceMainContent(f, "login", true);
    }

    void openEditScreen(Credentials credentials) {
        LoginFragment f = LoginFragment.newInstance(credentials);
        fragmentManagerOwner.replaceMainContent(f, "login", true);
    }

    void removeDevice(Credentials credentials) {
        appSettings.removeCredentials(credentials);
        reload();
    }

    void exitActivity() {
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, null);
    }

    void setAsDefault(Credentials credentials) {
        appSettings.setDefaultCredentials(credentials);
        reload();
    }

}
