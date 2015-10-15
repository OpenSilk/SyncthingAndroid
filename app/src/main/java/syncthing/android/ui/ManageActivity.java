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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ActivityResultsActivity;
import org.opensilk.common.ui.mortar.ActivityResultsOwner;
import org.opensilk.common.ui.mortar.ToolbarOwner;
import org.opensilk.common.ui.mortar.ToolbarOwnerDelegate;
import org.opensilk.common.ui.mortarfragment.MortarFragment;
import org.opensilk.common.ui.mortarfragment.MortarFragmentActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mortar.MortarScope;
import syncthing.android.AppComponent;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.login.LoginFragment;
import syncthing.android.ui.login.ManageFragment;
import syncthing.android.ui.welcome.WelcomeFragment;
import timber.log.Timber;

/**
 * Created by drew on 3/10/15.
 */
public class ManageActivity extends MortarFragmentActivity implements ActivityResultsActivity {

    public static final String EXTRA_CREDENTIALS = "credentials";
    public static final String EXTRA_ARGS = "args";
    public static final String EXTRA_FRAGMENT = "fragment";
    public static final String EXTRA_DISABLE_BACK = "disableback";

    @Inject ActivityResultsOwner mActivityResultsOwner;
    @Inject ToolbarOwner mToolbarOwner;
    ToolbarOwnerDelegate<ManageActivity> mToolbarOwnerDelegate;

    @InjectView(R.id.toolbar) Toolbar mToolbar;

    private boolean ignoreBackButton;

    @Override
    protected void onCreateScope(MortarScope.Builder builder) {
        AppComponent component = DaggerService.getDaggerComponent(getApplicationContext());
        builder.withService(DaggerService.DAGGER_SERVICE, ManageActivityComponent.FACTORY.call(component));
    }

    @Override
    protected void onScopeCreated(MortarScope scope) {

    }

    @Override
    protected void performInjection() {
        DaggerService.<ManageActivityComponent>getDaggerComponent(this).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        setResult(RESULT_CANCELED);
        mActivityResultsOwner.takeView(this);
        mToolbarOwnerDelegate = new ToolbarOwnerDelegate<ManageActivity>(this, mToolbarOwner, null);
        mToolbarOwnerDelegate.onCreate();
        mToolbarOwner.attachToolbar(mToolbar);
        mToolbarOwner.setConfig(ActionBarConfig.builder().setTitle(R.string.welcome_title).build());
        if (savedInstanceState == null) {
            MortarFragment fragment = MortarFragment.factory(this,
                    getIntent().getStringExtra(EXTRA_FRAGMENT),
                    getIntent().getBundleExtra(EXTRA_ARGS));
            if (getIntent().getBundleExtra(EXTRA_ARGS) == null) {
                Timber.e("bundle args were null");
            }
            ignoreBackButton = getIntent().getBooleanExtra(EXTRA_DISABLE_BACK, false);
            mFragmentManagerOwner.replaceMainContent(fragment, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityResultsOwner.dropView(this);
        mToolbarOwner.detachToolbar(mToolbar);
        mToolbarOwnerDelegate.onDestroy();
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
    public void onBackPressed() {
        if (!ignoreBackButton) {
            super.onBackPressed();
        }
    }

    /*
     * ToolbarOwner
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mToolbarOwnerDelegate.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mToolbarOwnerDelegate.onOptionsItemSelected(item);
    }

    /*
     * FragmentManagerOwner Activity
     */

    @Override
    public int getContainerViewId() {
        return R.id.main;
    }

    /*
     * ActivityResultsOwnerActivity
     */

    @Override
    public void setResultAndFinish(int resultCode, Intent data) {
        setResult(resultCode, data);
        finish();
    }

}
