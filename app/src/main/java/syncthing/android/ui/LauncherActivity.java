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
import org.opensilk.common.ui.mortar.ActionBarOwner;
import org.opensilk.common.ui.mortar.ActionBarOwnerDelegate;
import org.opensilk.common.ui.mortar.ActivityResultsActivity;
import org.opensilk.common.ui.mortar.ActivityResultsOwner;
import org.opensilk.common.ui.mortar.DrawerOwner;
import org.opensilk.common.ui.mortar.DrawerOwnerActivity;
import org.opensilk.common.ui.mortarfragment.MortarFragmentActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import mortar.MortarScope;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
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
        DrawerOwnerActivity, ActivityResultsActivity {

    @Inject ActionBarOwner mActionBarOwner;
    @Inject DrawerOwner mDrawerOwner;
    @Inject AppSettings mSettings;
    @Inject ActivityResultsOwner mActivityResultsOwner;

    ActionBarDrawerToggle mDrawerToggle;
    protected ActionBarOwnerDelegate<LauncherActivity> mActionBarOwnerDelegate;
    Subscription mChargingSubscription;

    @InjectView(R.id.drawer_layout) @Optional DrawerLayout mDrawerLayout;
    @InjectView(R.id.drawer) ViewGroup mDrawer;
    @InjectView(R.id.toolbar) Toolbar mToolbar;
    @InjectView(R.id.main) ViewGroup mMain;

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
        mActionBarOwnerDelegate = new ActionBarOwnerDelegate<>(this, mActionBarOwner, mToolbar);
        mActionBarOwnerDelegate.onCreate();

        if (mDrawerLayout != null) {
            mDrawerToggle = new Toggle(this, mDrawerLayout, mToolbar);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerOwner.takeView(this);
        }

        if (mSettings.keepScreenOn()) {
            subscribeChargingState();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityResultsOwner.dropView(this);
        mActionBarOwnerDelegate.onDestroy();
        mDrawerOwner.dropView(this);//Noop if no view taken
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
        if (mDrawerToggle != null) mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mActionBarOwnerDelegate.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) ||
                mActionBarOwnerDelegate.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item);
    }

    /*
     * Drawer ACtivity
     */

    @Override
    public void openDrawer() {
        if (!isDrawerOpen()) mDrawerLayout.openDrawer(mDrawer);
    }

    public void closeDrawer() {
        if (isDrawerOpen()) mDrawerLayout.closeDrawer(mDrawer);
    }

    @Override
    public void disableDrawer(boolean hideIndicator) {
        if (mDrawerToggle != null) mDrawerToggle.setDrawerIndicatorEnabled(!hideIndicator);
        closeDrawer();
        if (mDrawerLayout != null) mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mDrawer);
    }

    @Override
    public void enableDrawer() {
        if (mDrawerToggle != null) mDrawerToggle.setDrawerIndicatorEnabled(true);
        if (mDrawerLayout != null) mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mDrawer);
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
     * Battery
     */

    void subscribeChargingState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
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
    }

}
