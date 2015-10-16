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

package syncthing.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ActivityResultsActivity;
import org.opensilk.common.ui.mortar.ActivityResultsOwner;
import org.opensilk.common.ui.mortar.DrawerOwner;
import org.opensilk.common.ui.mortar.DrawerOwnerActivity;
import org.opensilk.common.ui.mortar.DrawerOwnerDelegate;
import org.opensilk.common.ui.mortar.ToolbarOwner;
import org.opensilk.common.ui.mortar.ToolbarOwnerDelegate;
import org.opensilk.common.ui.mortarfragment.MortarFragment;
import org.opensilk.common.ui.mortarfragment.MortarFragmentActivity;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import mortar.MortarScope;
import rx.Subscription;
import syncthing.android.AppComponent;
import syncthing.android.AppSettings;
import syncthing.android.R;
import syncthing.android.identicon.IdenticonGenerator;
import syncthing.android.model.Credentials;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ActivityRequestCodes;
import syncthing.android.ui.login.LoginFragment;
import syncthing.android.ui.login.ManageFragment;
import syncthing.android.ui.session.SessionFragment;
import syncthing.android.ui.settings.SettingsActivity;
import syncthing.android.ui.welcome.WelcomeFragment;
import timber.log.Timber;

/**
 * Created by drew on 3/1/15.
 */
public class LauncherActivity extends SyncthingActivity implements
        DrawerOwnerActivity {

    @Inject DrawerOwner mDrawerOwner;
    @Inject IdenticonGenerator mIdenticonGenerator;

    ActionBarDrawerToggle mDrawerToggle;
    protected DrawerOwnerDelegate<LauncherActivity> mDrawerOwnerDelegate;

    @InjectView(R.id.drawer_layout) @Optional DrawerLayout mDrawerLayout;
    @InjectView(R.id.navigation) NavigationView mNavigation;

    @Override
    protected void onCreateScope(MortarScope.Builder builder) {
        AppComponent component = DaggerService.getDaggerComponent(getApplicationContext());
        builder.withService(DaggerService.DAGGER_SERVICE, LauncherActivityComponent.FACTORY.call(component));
    }

    @Override
    protected void performInjection() {
        DaggerService.<LauncherActivityComponent>getDaggerComponent(this).inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("-> onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.inject(this);


        if (mDrawerLayout != null) {
            mDrawerOwnerDelegate = new DrawerOwnerDelegate<>(this, mDrawerOwner, mDrawerLayout,
                    R.string.app_name, R.string.app_name);
            mDrawerOwnerDelegate.onCreate();
            mActionBarOwnerDelegate = new ToolbarOwnerDelegate<>(this, mActionBarOwner, mDrawerOwnerDelegate);
        } else {
            mActionBarOwnerDelegate = new ToolbarOwnerDelegate<>(this, mActionBarOwner);
        }
        mActionBarOwnerDelegate.onCreate();


        setupNavigation(savedInstanceState);
        Timber.d("<- onCreate()");
    }

    @Override
    protected void onDestroy() {
        Timber.d("-> onDestroy()");
        super.onDestroy();
        if (mDrawerOwnerDelegate != null) mDrawerOwnerDelegate.onDestroy();
        Timber.d("<- onDestroy()");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerOwnerDelegate != null) mDrawerOwnerDelegate.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerOwnerDelegate != null) mDrawerOwnerDelegate.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (mDrawerOwnerDelegate != null && mDrawerOwnerDelegate.onOptionsItemSelected(item))
                || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerOwnerDelegate != null && mDrawerOwnerDelegate.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    /*
     * Drawer ACtivity
     */


    @Override
    public void openDrawer(int gravity) {
        if (!isDrawerOpen()) mDrawerLayout.openDrawer(gravity);
    }

    @Override
    public void openDrawers() {
        openDrawer(GravityCompat.START);
    }

    @Override
    public void closeDrawer(int gravity) {
        if (isDrawerOpen()) mDrawerLayout.closeDrawer(gravity);
    }

    @Override
    public void closeDrawers() {
        closeDrawer(GravityCompat.START);
    }

    @Override
    public void enableDrawer(int gravity, boolean enable) {
        int mode = enable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        if (mDrawerToggle != null) mDrawerToggle.setDrawerIndicatorEnabled(enable);
        if (mDrawerLayout != null) mDrawerLayout.setDrawerLockMode(mode, gravity);
    }

    @Override
    public void enableDrawers(boolean enable) {
        enableDrawer(GravityCompat.START, enable);
    }

    /*
     * drawer helpers
     */

    private boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult(%d, %d, %s)", requestCode, resultCode, data);
        final Handler handler = new Handler();
        switch (requestCode) {
            case ActivityRequestCodes.LOGIN_ACTIVITY:
            case ActivityRequestCodes.MANAGE_ACTIVITY:
            case ActivityRequestCodes.WELCOME_ACTIVITY: {
                // OK with credentials opens the device
                if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(ManageActivity.EXTRA_CREDENTIALS)) {
                    Timber.d("Got Positive response");
                    data.setExtrasClassLoader(LauncherActivity.class.getClassLoader());
                    final Credentials currentDevice = data.getParcelableExtra(ManageActivity.EXTRA_CREDENTIALS);
                    if (currentDevice != null) {
                        Timber.d("Found credentials in the intent");
                        handler.postDelayed(() -> populateNavigationMenu(currentDevice, true), 10);
                        return;
                    }
                }
                Timber.d("Result either canceled or missing credentials");
                //IDK just open the drawer
                handler.postDelayed(() -> {
                    populateNavigationMenu(null, false);
                    openDrawer(GravityCompat.START);
                }, 10);
                break;
            }
            default:
                Timber.d("Unknown request code");
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    void setupNavigation(Bundle savedInstanceState) {
        mNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.manage_devices:
                        closeDrawers();
                        startDeviceManageActivity();
                        return true;
                    case R.id.app_settings:
                        closeDrawers();
                        startSettingsActivity();
                        return true;
                    default:
                        break;
                }
                Intent intent = item.getIntent();
                if (intent != null && intent.hasExtra(ManageActivity.EXTRA_CREDENTIALS)) {
                    item.setChecked(true);
                    closeDrawers();

                    intent.setExtrasClassLoader(LauncherActivity.class.getClassLoader());
                    Credentials credentials = intent.getParcelableExtra(ManageActivity.EXTRA_CREDENTIALS);
                    openSession(credentials);
                    return true;
                }
                return false;
            }
        });
        Credentials current = mSettings.getDefaultCredentials();
        populateNavigationMenu(current, savedInstanceState == null);
    }

    void populateNavigationMenu(Credentials current, boolean goToCurrent) {
        List<Credentials> creds = mSettings.getSavedCredentialsSorted();
        //TODO any way to just clear the devices group?
        mNavigation.getMenu().clear();
        mNavigation.inflateMenu(R.menu.navigation);
        Menu menu = mNavigation.getMenu();
        for (int ii=0; ii<creds.size();ii++) {
            Credentials c = creds.get(ii);
            MenuItem item = menu.add(R.id.nav_devices, Menu.NONE, ii+1, c.alias);
            item.setIcon(new BitmapDrawable(getResources(), mIdenticonGenerator.generate(c.id)));
            item.setIntent(new Intent().putExtra(ManageActivity.EXTRA_CREDENTIALS, c));
            if (goToCurrent && c.equals(current)) {
                item.setChecked(true);
            }
        }
        if (goToCurrent) {
            if (current != null) {
                openSession(current);
            } else if (creds.isEmpty()) {
                startWelcomeActivity();
            } else {
                //???
                Timber.w("Ignoring goToCurrent");
            }
        }
    }

    void openSession(Credentials credentials) {
        Timber.d("opening session %s", credentials.alias);
        MortarFragment session = SessionFragment.newInstance(credentials);
        int ret = mFragmentManagerOwner.replaceMainContent(session, false);
    }

    void startDeviceManageActivity() {
        Intent intent = new Intent(this, ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, ManageFragment.NAME);
        mActivityResultsOwner.startActivityForResult(intent, ActivityRequestCodes.MANAGE_ACTIVITY, null);
    }

    void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        mActivityResultsOwner.startActivityForResult(intent, 0, null);
    }

    void startLoginActivity() {
        Intent intent = new Intent(this, ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, LoginFragment.NAME);
        mActivityResultsOwner.startActivityForResult(intent, ActivityRequestCodes.LOGIN_ACTIVITY, null);
    }

    void startWelcomeActivity() {
        Intent intent = new Intent(this, ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, WelcomeFragment.NAME)
                .putExtra(ManageActivity.EXTRA_DISABLE_BACK, true);
        mActivityResultsOwner.startActivityForResult(intent, ActivityRequestCodes.WELCOME_ACTIVITY, null);
    }

}
