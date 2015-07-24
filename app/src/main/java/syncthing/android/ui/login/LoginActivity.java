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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActivityResultsActivity;
import org.opensilk.common.ui.mortar.ActivityResultsOwner;
import org.opensilk.common.ui.mortarfragment.MortarFragmentActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mortar.MortarScope;
import syncthing.android.AppComponent;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.welcome.WelcomeFragment;

/**
 * Created by drew on 3/10/15.
 */
public class LoginActivity extends MortarFragmentActivity implements ActivityResultsActivity {

    public static final String ACTION_MANAGE = "manage";
    public static final String ACTION_WELCOME = "welcome";
    public static final String EXTRA_CREDENTIALS = "creds";
    public static final String EXTRA_FROM = "from";

    @Inject ActivityResultsOwner mActivityResultsOwner;

    @InjectView(R.id.toolbar) Toolbar mToolbar;
    Fragment fragment;

    @Override
    protected void onCreateScope(MortarScope.Builder builder) {
        AppComponent component = DaggerService.getDaggerComponent(getApplicationContext());
        builder.withService(DaggerService.DAGGER_SERVICE, LoginActivityComponent.FACTORY.call(component));
    }

    @Override
    protected void performInjection() {
        DaggerService.<LoginActivityComponent>getDaggerComponent(this).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        setResult(RESULT_CANCELED);
        mActivityResultsOwner.takeView(this);
        if (savedInstanceState == null) {
            if (ACTION_WELCOME.equals(getIntent().getAction())) {
                fragment = WelcomeFragment.newInstance();
            } else if (ACTION_MANAGE.equals(getIntent().getAction())) {
                fragment = ManageFragment.newInstance();
            } else {
                getIntent().setExtrasClassLoader(getClass().getClassLoader());
                fragment = LoginFragment.newInstance(getIntent().getParcelableExtra(EXTRA_CREDENTIALS));
            }
            mFragmentManagerOwner.replaceMainContent(fragment, fragment.getClass().getName(), false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityResultsOwner.dropView(this);
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
        if (fragment instanceof WelcomeFragment) {
            return;
        }
        super.onBackPressed();
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
