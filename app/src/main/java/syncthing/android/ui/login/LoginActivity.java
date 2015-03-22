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
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import org.opensilk.common.mortarfragment.MortarFragmentActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mortar.MortarScope;
import mortar.dagger2support.DaggerService;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;

/**
 * Created by drew on 3/10/15.
 */
public class LoginActivity extends MortarFragmentActivity {

    public static final String ACTION_MANAGE = "manage";
    public static final String EXTRA_CREDENTIALS = "creds";

    @InjectView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreateScope(MortarScope.Builder builder) {
        builder.withService(DaggerService.SERVICE_NAME,
                DaggerService.createComponent(LoginActivityComponent.class,
                        DaggerService.getDaggerComponent(getApplicationContext()), new LoginActivityModule()));
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
        if (savedInstanceState == null) {
            Fragment f;
            if (ACTION_MANAGE.equals(getIntent().getAction())) {
                f = ManageFragment.newInstance();
            } else {
                f = LoginFragment.newInstance(getIntent().getParcelableExtra(EXTRA_CREDENTIALS));
            }
            mFragmentManagerOwner.replaceMainContent(f, f.getClass().getName(), false);
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

    /*
     * FragmentManagerOwner Activity
     */

    @Override
    public int getContainerViewId() {
        return R.id.main;
    }

}
