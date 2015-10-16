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

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.opensilk.common.ui.mortar.ActivityResultsActivity;
import org.opensilk.common.ui.mortar.ActivityResultsOwner;
import org.opensilk.common.ui.mortar.DialogFactory;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortar.DialogPresenterActivity;
import org.opensilk.common.ui.mortar.ToolbarOwner;
import org.opensilk.common.ui.mortar.ToolbarOwnerDelegate;
import org.opensilk.common.ui.mortarfragment.MortarFragmentActivity;

import javax.inject.Inject;

import syncthing.android.AppSettings;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import timber.log.Timber;

/**
 * Created by drew on 10/16/15.
 */
public abstract class SyncthingActivity extends MortarFragmentActivity implements
        ActivityResultsActivity, ToolbarOwnerDelegate.Callback, DialogPresenterActivity {

    @Inject ToolbarOwner mActionBarOwner;
    @Inject AppSettings mSettings;
    @Inject ActivityResultsOwner mActivityResultsOwner;
    @Inject DialogPresenter mDialogPresenter;

    protected ToolbarOwnerDelegate<SyncthingActivity> mActionBarOwnerDelegate;
    private Dialog mActiveDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityResultsOwner.takeView(this);
        mDialogPresenter.takeView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityResultsOwner.dropView(this);
        mActionBarOwnerDelegate.onDestroy();
        mDialogPresenter.dropView(this);
        dismissDialog();
    }

    @Override
    protected void onStart() {
        Timber.d("-> onStart()");
        super.onStart();
        SyncthingUtils.notifyForegroundStateChanged(this, true);
        if (mSettings.keepScreenOn()) {
            subscribeChargingState();
        }
        Timber.d("<- onStart()");
    }

    @Override
    protected void onStop() {
        Timber.d("-> onStop");
        super.onStop();
        SyncthingUtils.notifyForegroundStateChanged(this, false);
        unsubscribeChargingState();
        Timber.d("<- onStop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mActionBarOwnerDelegate.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActionBarOwnerDelegate.onOptionsItemSelected(item)
                || super.onOptionsItemSelected(item);
    }

        /*
     * ActivityResultsOwverActivity
     */

    @Override
    public void setResultAndFinish(int resultCode, Intent data) {
        setResult(resultCode, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mActivityResultsOwner.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
     * FragmentManagerOwner.Activity
     */

    @Override
    public int getContainerViewId() {
        return R.id.main;
    }


    /*
     * DialogActivity
     */

    @Override
    public void showDialog(DialogFactory factory) {
        dismissDialog();
        mActiveDialog = factory.call(this);
        mActiveDialog.show();
    }

    @Override
    public void dismissDialog() {
        if (mActiveDialog != null) {
            mActiveDialog.dismiss();
            mActiveDialog = null;
        }
    }

    /*
     * Battery
     */

    final BroadcastReceiver mChargingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
            Timber.d("received BATTERY_CHANGED plugged=%s", status != 0);
            if (mSettings.keepScreenOn() && status != 0) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    };

    void subscribeChargingState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mChargingReceiver, filter);
    }

    void unsubscribeChargingState() {
        try {
            unregisterReceiver(mChargingReceiver);
        } catch (Exception e) {//i think its illegal state but cant remember (and dont care)
            //pass
        }
    }
}
