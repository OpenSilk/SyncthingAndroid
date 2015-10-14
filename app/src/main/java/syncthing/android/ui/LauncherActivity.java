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
public class LauncherActivity extends MortarFragmentActivity implements
        DrawerOwnerActivity, ActivityResultsActivity, ToolbarOwnerDelegate.Callback {

    @Inject ToolbarOwner mActionBarOwner;
    @Inject DrawerOwner mDrawerOwner;
    @Inject AppSettings mSettings;
    @Inject ActivityResultsOwner mActivityResultsOwner;
    @Inject IdenticonGenerator mIdenticonGenerator;

    ActionBarDrawerToggle mDrawerToggle;
    protected DrawerOwnerDelegate<LauncherActivity> mDrawerOwnerDelegate;
    protected ToolbarOwnerDelegate<LauncherActivity> mActionBarOwnerDelegate;
    Subscription mChargingSubscription;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.inject(this);

        mActivityResultsOwner.takeView(this);

        mActionBarOwner.setConfig(ActionBarConfig.builder().setTitle("").build());

        if (mDrawerLayout != null) {
            mDrawerOwnerDelegate = new DrawerOwnerDelegate<>(this, mDrawerOwner, mDrawerLayout,
                    R.string.app_name, R.string.app_name);
            mDrawerOwnerDelegate.onCreate();
            mActionBarOwnerDelegate = new ToolbarOwnerDelegate<>(this, mActionBarOwner, mDrawerOwnerDelegate);
        } else {
            mActionBarOwnerDelegate = new ToolbarOwnerDelegate<>(this, mActionBarOwner);
        }
        mActionBarOwnerDelegate.onCreate();

        if (mSettings.keepScreenOn()) {
            subscribeChargingState();
        }

        setupNavigation(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityResultsOwner.dropView(this);
        mActionBarOwnerDelegate.onDestroy();
        if (mDrawerOwnerDelegate != null) mDrawerOwnerDelegate.onDestroy();
        if (mChargingSubscription != null) {
            mChargingSubscription.unsubscribe();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SyncthingUtils.notifyForegroundStateChanged(this, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SyncthingUtils.notifyForegroundStateChanged(this, false);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return mActionBarOwnerDelegate.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (mDrawerOwnerDelegate != null && mDrawerOwnerDelegate.onOptionsItemSelected(item))
                || mActionBarOwnerDelegate.onOptionsItemSelected(item)
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

    /*
     * FragmentManagerOwner.Activity
     */

    @Override
    public int getContainerViewId() {
        return R.id.main;
    }

    /*
     * ActivityResultsOwverActivity
     */

    @Override
    public void setResultAndFinish(int resultCode, Intent data) {
        setResult(resultCode, data);
        finish();
    }

    /*
     * Toolbar
     */
    @Override
    public void onToolbarAttached(Toolbar toolbar) {

    }

    @Override
    public void onToolbarDetached(Toolbar toolbar) {

    }

    /*
     * Battery
     */

    void subscribeChargingState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        /*TODO fix this
        mChargingSubscription = AndroidObservable.bindActivity(this,AndroidObservable.fromBroadcast(this, filter))
                .subscribe(new Action1<Intent>() {
                               @Override
                               public void call(Intent intent) {
                                   int status = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
                                   Timber.d("received BATTERY_CHANGED plugged=%s", status != 0);
                                   if (mSettings.keepScreenOn() && status != 0) {
                                       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                   } else {
                                       getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                   }
                               }
                           }
                );
                */
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult(%d, %d, %s)", requestCode, resultCode, data);
        if (requestCode == ActivityRequestCodes.LOGIN_ACTIVITY) {
            // OK with credentials opens the device
            if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(ManageActivity.EXTRA_CREDENTIALS)) {
                data.setExtrasClassLoader(LauncherActivity.class.getClassLoader());
                Credentials currentDevice = data.getParcelableExtra(ManageActivity.EXTRA_CREDENTIALS);
                if (currentDevice != null) {
                    populateNavigationMenu(currentDevice, true);
                }
            } else {
                // Cancelled login
                startWelcomeActivity();
            }
        } else if (requestCode == ActivityRequestCodes.MANAGE_ACTIVITY) {
            if (mSettings.getSavedCredentials().isEmpty()) {
                populateNavigationMenu(null, true);
            }
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
            if (current == null) {
                if (creds.isEmpty()) {
                    startWelcomeActivity();
                } else {
                    startLoginActivity();
                }
            } else {
                openSession(current);
            }
        }
    }

    void openSession(Credentials credentials) {
        MortarFragment session = SessionFragment.newInstance(credentials);
        mFragmentManagerOwner.replaceMainContent(session, false);
    }

    void startDeviceManageActivity() {
        Intent intent = new Intent(this, ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, ManageFragment.NAME);
        startActivityForResult(intent, ActivityRequestCodes.MANAGE_ACTIVITY, null);
    }

    void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 0, null);
    }

    void startLoginActivity() {
        Intent intent = new Intent(this, ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, LoginFragment.NAME);
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_ACTIVITY, null);
    }

    void startWelcomeActivity() {
        Intent intent = new Intent(this, ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, WelcomeFragment.NAME)
                .putExtra(ManageActivity.EXTRA_DISABLE_BACK, true);
        startActivityForResult(intent, ActivityRequestCodes.WELCOME_ACTIVITY, null);
    }

}
