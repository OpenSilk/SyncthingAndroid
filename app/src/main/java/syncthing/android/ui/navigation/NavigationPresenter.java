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

package syncthing.android.ui.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.opensilk.common.dagger2.ForApplication;
import org.opensilk.common.mortar.ActivityResultsController;
import org.opensilk.common.mortar.ActivityResultsListener;
import org.opensilk.common.mortar.DrawerOwner;
import org.opensilk.common.mortarfragment.FragmentManagerOwner;

import java.util.List;

import javax.inject.Inject;

import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import syncthing.android.AppSettings;
import syncthing.android.identicon.IdenticonGenerator;
import syncthing.android.model.Credentials;
import syncthing.android.ui.common.ActivityRequestCodes;
import syncthing.android.ui.login.LoginActivity;
import syncthing.android.ui.session.SessionFragment;
import syncthing.android.ui.settings.SettingsActivity;
import timber.log.Timber;

/**
* Created by drew on 3/11/15.
*/
@NavigationScope
public class NavigationPresenter extends ViewPresenter<NavigationScreenView> implements ActivityResultsListener {

    final FragmentManagerOwner fragmentManagerOwner;
    final DrawerOwner drawerOwner;
    final IdenticonGenerator identiconGenerator;
    final ActivityResultsController activityResultsController;
    final AppSettings appSettings;
    final Context appContext;

    Credentials currentDevice;

    @Inject
    public NavigationPresenter(
            FragmentManagerOwner fragmentManagerOwner,
            DrawerOwner drawerOwner,
            IdenticonGenerator identiconGenerator,
            ActivityResultsController activityResultsController,
            AppSettings appSettings,
            @ForApplication Context context
    ) {
        this.fragmentManagerOwner = fragmentManagerOwner;
        this.drawerOwner = drawerOwner;
        this.identiconGenerator = identiconGenerator;
        this.activityResultsController = activityResultsController;
        this.appSettings = appSettings;
        this.appContext = context;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        activityResultsController.register(scope, this);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        Timber.d("onLoad");
        super.onLoad(savedInstanceState);
        if (savedInstanceState != null) {
            Timber.d("from saved instance");
            currentDevice = savedInstanceState.getParcelable("current");
        } else {
            currentDevice = appSettings.getDefaultCredentials();
        }
        reload(savedInstanceState == null);
    }

    @Override
    protected void onSave(Bundle outState) {
        Timber.d("onSave");
        super.onSave(outState);
        outState.putParcelable("current", currentDevice);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCodes.LOGIN_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(LoginActivity.EXTRA_CREDENTIALS)) {
                currentDevice = data.getParcelableExtra(LoginActivity.EXTRA_CREDENTIALS);
                if (hasView()) reload(true);
                return true;
            } else {
                //could have changed
                currentDevice = appSettings.getDefaultCredentials();
                if (hasView()) reload(false);
            }
        }
        return false;
    }

    void reload(boolean gotoCurrent) {
        List<Credentials> creds = appSettings.getSavedCredentialsSorted();
        getView().load(creds);
        if (gotoCurrent) {
            if (creds.isEmpty()) {
                startLoginActivity();//TODO not sure best way to handle this
            } else {
                postOpenCurrentDevice();
            }
        }
    }

    void postOpenCurrentDevice() {
        if (currentDevice != null) {
            //Protect fragment manager illegal state bullshit
            final Scheduler.Worker worker = AndroidSchedulers.mainThread().createWorker();
            worker.schedule(() -> {
                openSessionScreen(currentDevice);
                worker.unsubscribe();
            });
        }
    }

    void openSessionScreen(Credentials credentials) {
        Timber.d("opening session for %s", credentials.alias);
        doFragmentReplace(SessionFragment.newInstance(credentials), credentials.alias);
    }

    void doFragmentReplace(Fragment fragment, String tag) {
        drawerOwner.closeDrawer();
        fragmentManagerOwner.replaceMainContent(fragment, tag, false);
    }

    void startDeviceManageActivity() {
        drawerOwner.closeDrawer();
        Intent intent = new Intent(appContext, LoginActivity.class).setAction(LoginActivity.ACTION_MANAGE);
        activityResultsController.startActivityForResult(intent, ActivityRequestCodes.LOGIN_ACTIVITY, null);
    }

    void startSettingsActivity() {
        drawerOwner.closeDrawer();
        Intent intent = new Intent(appContext, SettingsActivity.class);
        activityResultsController.startActivityForResult(intent, 0, null);
    }

    void startLoginActivity() {
        drawerOwner.closeDrawer();
        Intent intent = new Intent(appContext, LoginActivity.class);
        activityResultsController.startActivityForResult(intent, ActivityRequestCodes.LOGIN_ACTIVITY, null);
    }

}
