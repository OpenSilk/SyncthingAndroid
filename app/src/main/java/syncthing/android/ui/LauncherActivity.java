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
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ActivityResultsActivity;
import org.opensilk.common.ui.mortar.ActivityResultsOwner;
import org.opensilk.common.ui.mortar.DrawerOwner;
import org.opensilk.common.ui.mortar.DrawerOwnerActivity;
import org.opensilk.common.ui.mortar.DrawerOwnerDelegate;
import org.opensilk.common.ui.mortar.ToolbarOwner;
import org.opensilk.common.ui.mortar.ToolbarOwnerDelegate;
import org.opensilk.common.ui.mortarfragment.MortarFragmentActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import mortar.MortarScope;
import rx.Subscription;
import rx.functions.Action1;
import syncthing.android.AppComponent;
import syncthing.android.AppSettings;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
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

    ActionBarDrawerToggle mDrawerToggle;
    protected DrawerOwnerDelegate<LauncherActivity> mDrawerOwnerDelegate;
    protected ToolbarOwnerDelegate<LauncherActivity> mActionBarOwnerDelegate;
    Subscription mChargingSubscription;

    @InjectView(R.id.drawer_layout) @Optional DrawerLayout mDrawerLayout;
    @InjectView(R.id.drawer) ViewGroup mDrawer;

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
        return mDrawer != null && mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mDrawer);
    }

    /*
     * FragmentManagerOwner.Activity
     */

    @Override
    public int getContainerViewId() {
        return R.id.main;
    }

    class Toggle extends ActionBarDrawerToggle {
        public Toggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar) {
            super(activity, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
        }

        @Override
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
        }
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

}
