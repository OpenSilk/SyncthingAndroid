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
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opensilk.common.mortar.ActionBarOwner;
import org.opensilk.common.mortar.DrawerOwner;
import org.opensilk.common.mortarfragment.MortarFragmentActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import mortar.MortarScope;
import mortar.dagger2support.DaggerService;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;

/**
 * Created by drew on 3/1/15.
 */
public class LauncherActivity extends MortarFragmentActivity implements
        ActionBarOwner.Activity, DrawerOwner.Activity {

    @Inject ActionBarOwner mActionBarOwner;
    @Inject DrawerOwner mDrawerOwner;

    ActionBarDrawerToggle mDrawerToggle;
    protected ActionBarOwner.MenuConfig mMenuConfig;


    @InjectView(R.id.drawer_layout) @Optional DrawerLayout mDrawerLayout;
    @InjectView(R.id.drawer) ViewGroup mDrawer;
    @InjectView(R.id.toolbar) Toolbar mToolbar;
    @InjectView(R.id.main) ViewGroup mMain;

    @Override
    protected void onCreateScope(MortarScope.Builder builder) {
        builder.withService(DaggerService.SERVICE_NAME,
                DaggerService.createComponent(LauncherActivityComponent.class,
                        new LauncherActivityModule(), DaggerService.getDaggerComponent(getApplicationContext())));
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

        setSupportActionBar(mToolbar);
        setTitle("");
        mActionBarOwner.takeView(this);

        if (mDrawerLayout != null) {
            mDrawerToggle = new Toggle(this, mDrawerLayout);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerOwner.takeView(this);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//TODO temporary

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActionBarOwner.dropView(this);
        mDrawerOwner.dropView(this);//Noop if no view taken
        mMenuConfig = null;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        return (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
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
     * ActionBarOwner.Activity
     */

    @Override
    public void setUpButtonEnabled(boolean enabled) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
    }

    @Override
    public void setTitle(int titleId) {
        getSupportActionBar().setTitle(titleId);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void setSubtitle(int subTitleRes) {
        getSupportActionBar().setSubtitle(subTitleRes);
    }

    @Override
    public void setSubtitle(CharSequence title) {
        getSupportActionBar().setSubtitle(title);
    }

    @Override
    public void setMenu(ActionBarOwner.MenuConfig menuConfig) {
        mMenuConfig = menuConfig;
        supportInvalidateOptionsMenu();
    }

    @Override
    public void setTransparentActionbar(boolean yes) {
//        mToolbar.getBackground().setAlpha(yes ? 0 : 255);
    }

    /*
     * FragmentManagerOwner.Activity
     */

    @Override
    public int getContainerViewId() {
        return R.id.main;
    }

    /*
    public void onApiChange(SyncthingService.State currentState) {
        if (currentState != SyncthingService.State.ACTIVE && !isFinishing()) {
            if (currentState == SyncthingService.State.DISABLED) {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
                mDisabledDialog = SyncthingService.showDisabledDialog(this);
            } else if (mLoadingDialog == null) {
                final SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.loading_dialog, null);
                TextView loadingText = (TextView) dialogLayout.findViewById(R.id.loading_text);
//                loadingText.setText((getService().isFirstStart())
//                        ? R.string.web_gui_creating_key
//                        : R.string.api_loading);

                mLoadingDialog = new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setView(dialogLayout)
                        .show();

                // Make sure the first start dialog is shown on top.
                if (prefs.getBoolean("first_start", true)) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.welcome_title)
                            .setMessage(R.string.welcome_text)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    prefs.edit().putBoolean("first_start", false).commit();
                                }
                            })
                            .show();
                }
            }
            return;
        }

        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
        if (mDisabledDialog != null) {
            mDisabledDialog.dismiss();
        }
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
    */


    /**
     * Receives drawer opened and closed events.
     */
    class Toggle extends ActionBarDrawerToggle {
        public Toggle(Activity activity, DrawerLayout drawerLayout) {
            super(activity, drawerLayout, R.string.app_name, R.string.app_name);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
//            mDrawerFragment.onDrawerOpened();
        }

        @Override
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
//            mDrawerFragment.onDrawerClosed();
        }
    }

}
