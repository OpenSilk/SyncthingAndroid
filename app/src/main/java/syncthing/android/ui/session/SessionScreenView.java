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

package syncthing.android.ui.session;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.widget.Toast;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.recycler.RecyclerListFrame;

import java.util.List;

import javax.inject.Inject;

import butterknife.OnClick;
import syncthing.android.R;
import syncthing.android.ui.common.CanExpand;
import syncthing.android.ui.common.ExpandableCard;

/**
 * Created by drew on 3/6/15.
 */
public class SessionScreenView extends RecyclerListFrame {

    @Inject SessionPresenter mPresenter;

    SessionRecyclerAdapter mListAdapter;

    ProgressDialog mProgressDialog;
    AlertDialog mErrorDialog;

    public SessionScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            SessionComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mListAdapter = new SessionRecyclerAdapter();
        mListAdapter.setExpandListener((CanExpand.OnExpandListener) mList);
        mList.setAdapter(mListAdapter);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        //((CardRecyclerView) mList).setWobbleOnExpand(false);
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        dismissErrorDialog();
        dismissProgressDialog();
    }

    @OnClick(R.id.btn_retry)
    void doRetry() {
        mPresenter.retryConnection();
    }

    void initialize(List<ExpandableCard> notifs, List<FolderCard> folders, MyDeviceCard myDevice, List<DeviceCard> devices) {
        mListAdapter.setNotifications(notifs, false);
        mListAdapter.setFolders(folders, false);
        mListAdapter.setThisDevice(myDevice, false);
        mListAdapter.setDevices(devices, false);
        mListAdapter.notifyDataSetChanged();
    }

    void refreshNotifications(List<ExpandableCard> notifs) {
        mListAdapter.setNotifications(notifs, true);
    }

    void refreshFolders(List<FolderCard> folders) {
        mListAdapter.setFolders(folders, true);
    }

    void refreshThisDevice(MyDeviceCard myDevice) {
        mListAdapter.setThisDevice(myDevice, true);
    }

    void refreshDevices(List<DeviceCard> devices) {
        mListAdapter.setDevices(devices, true);
    }

    void showSavingDialog() {
        showProgressDialog(getResources().getString(R.string.saving_config_dots));
    }

    void dismissSavingDialog() {
        dismissProgressDialog();
    }

    void showRestartDialog() {
        showProgressDialog(getResources().getString(R.string.syncthing_is_restarting));
    }

    void dismissRestartDialog() {
        dismissProgressDialog();
    }

    void showProgressDialog(String msg) {
        dismissErrorDialog();
        dismissProgressDialog();
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(msg);
        mProgressDialog.show();
    }

    void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    void showErrorDialog(String title, String msg) {
        dismissErrorDialog();
        dismissProgressDialog();
        mErrorDialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    void dismissErrorDialog() {
        if (mErrorDialog != null && mErrorDialog.isShowing()) {
            mErrorDialog.dismiss();
        }
    }

    void showConfigSaved() {
        Toast.makeText(getContext(), R.string.config_saved, Toast.LENGTH_SHORT).show();
    }

}
