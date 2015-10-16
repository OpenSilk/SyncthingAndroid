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

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;
import org.opensilk.common.ui.recycler.RecyclerListFrame;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import syncthing.android.R;
import syncthing.android.ui.common.CanExpand;

/**
 * Created by drew on 3/6/15.
 */
public class SessionScreenView extends RecyclerListFrame implements ISessionScreenView {

    @Inject SessionPresenter mPresenter;
    @Inject ToolbarOwner mToolbarOwner;

    @InjectView(R.id.toolbar) Toolbar mToolbar;

    SessionRecyclerAdapter mListAdapter;
    boolean toolbarAttached;

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
        ButterKnife.inject(this);
        mListAdapter = new SessionRecyclerAdapter(mPresenter);
        mListAdapter.setExpandListener((CanExpand.OnExpandListener) mList);
        mList.setAdapter(mListAdapter);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        //((CardRecyclerView) mList).setWobbleOnExpand(false);
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mToolbarOwner.attachToolbar(mToolbar);
            toolbarAttached = true;
            mToolbarOwner.setConfig(mPresenter.getToolbarConfig());
            mPresenter.takeView(this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        mToolbarOwner.detachToolbar(mToolbar);
        toolbarAttached = false;
    }

    @OnClick(R.id.btn_retry)
    void doRetry() {
        mPresenter.retryConnection();
    }

    public void initialize(List<NotifCard> notifs, List<FolderCard> folders, MyDeviceCard myDevice, List<DeviceCard> devices) {
        mListAdapter.setNotifications(notifs, false);
        mListAdapter.setFolders(folders, false);
        mListAdapter.setThisDevice(myDevice, false);
        mListAdapter.setDevices(devices, false);
        mListAdapter.notifyDataSetChanged();
    }

    public void refreshNotifications(List<NotifCard> notifs) {
        mListAdapter.setNotifications(notifs, true);
    }

    public void refreshFolders(List<FolderCard> folders) {
        mListAdapter.setFolders(folders, true);
    }

    public void refreshThisDevice(MyDeviceCard myDevice) {
        mListAdapter.setThisDevice(myDevice, true);
    }

    public void refreshDevices(List<DeviceCard> devices) {
        mListAdapter.setDevices(devices, true);
    }

    @Override
    public void updateToolbarState(boolean show) {
        if (toolbarAttached) {
            mToolbarOwner.setConfig(mPresenter.getToolbarConfig());
        }
    }
}
