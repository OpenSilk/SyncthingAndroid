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
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;
import org.opensilk.common.ui.util.ViewUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import syncthing.android.R;
import syncthing.android.ui.common.CanExpand;
import syncthing.android.ui.common.CardViewHolder;
import syncthing.android.ui.common.ExpandCollapseHelper;
import syncthing.android.ui.common.ExpandableCard;
import timber.log.Timber;

/**
 * Created by drew on 10/11/15.
 */
public class SessionScreenViewLand extends CoordinatorLayout implements ISessionScreenView,
        CanExpand.OnExpandListener {
    @Inject SessionPresenter mPresenter;
    @Inject ToolbarOwner mToolbarOwner;

    @InjectView(R.id.toolbar) Toolbar mToolbar;
    @InjectView(R.id.scrollView) NestedScrollView mScrollView;
    @InjectView(R.id.lists_container) ViewGroup mListContainer;
    @InjectView(R.id.folders_container) ViewGroup mFoldersList;
    @InjectView(R.id.devicess_container) ViewGroup mDevicesList;
    @InjectView(R.id.notifications_container) ViewGroup mNotifList;
    @InjectView(R.id.empty_view) View mEmptyView;
    @InjectView(R.id.empty_text) TextView mEmptyText;
    @InjectView(R.id.loading_progress) ProgressBar mLoadingProgress;
    @InjectView(R.id.folders_header) HeaderCardView mFoldersHeader;
    @InjectView(R.id.devices_header) HeaderCardView mDevicesHeader;
    @InjectView(R.id.devices_wrappeer) ViewGroup mDevicesWrapper;

    HashMap<NotifCard, CardViewHolder> mNotifications = new LinkedHashMap<>();
    HashMap<FolderCard, CardViewHolder> mFolders = new LinkedHashMap<>();
    HashMap<DeviceCard, CardViewHolder> mDevices = new LinkedHashMap<>();
    CardViewHolder mMyDeviceVH;

    ProgressDialog mProgressDialog;
    AlertDialog mErrorDialog;

    public SessionScreenViewLand(Context context, AttributeSet attrs) {
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
        if (!isInEditMode()) {
            mToolbarOwner.attachToolbar(mToolbar);
            mPresenter.takeView(this);
        }
        mFoldersHeader.bind(HeaderCard.FOLDER);
        mDevicesHeader.bind(HeaderCard.DEVICE);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        mToolbarOwner.detachToolbar(mToolbar);
        dismissErrorDialog();
        dismissProgressDialog();
    }

    @Override
    public void onExpandStart(CanExpand viewCard, View expandingLayout) {
        ExpandCollapseHelper.animateExpanding(expandingLayout, viewCard, null, true);
    }

    @Override
    public void onCollapseStart(CanExpand viewCard, View expandingLayout) {
        ExpandCollapseHelper.animateCollapsing(expandingLayout, viewCard, null, true);
    }

    @OnClick(R.id.btn_retry)
    void doRetry() {
        mPresenter.retryConnection();
    }

    public void initialize(List<NotifCard> notifs, List<FolderCard> folders, MyDeviceCard myDevice, List<DeviceCard> devices) {
        refreshFolders(folders);
        refreshThisDevice(myDevice);
        refreshDevices(devices);
        refreshNotifications(notifs);
    }

    public void refreshNotifications(List<NotifCard> notifs) {
        refreshList(notifs, mNotifList, mNotifications);
    }

    public void refreshFolders(List<FolderCard> folders) {
        refreshList(folders, mFoldersList, mFolders);
    }

    public void refreshThisDevice(MyDeviceCard myDevice) {
        if (myDevice != null) {
            int idx;
            if (mMyDeviceVH != null && (idx = mDevicesWrapper.indexOfChild(mMyDeviceVH.itemView)) != -1) {
                mMyDeviceVH.bind(myDevice, this);
            } else {
                myDevice.setExpanded(true);
                View v = ViewUtils.inflate(getContext(), myDevice.getLayout(), mDevicesWrapper, false);
                mMyDeviceVH = new CardViewHolder(v, mPresenter);
                mMyDeviceVH.bind(myDevice, this);
                mDevicesWrapper.addView(v, 0);
            }
        }
    }

    public void refreshDevices(List<DeviceCard> devices) {
        refreshList(devices, mDevicesList, mDevices);
    }

    <T extends ExpandableCard>  void refreshList(List<T> list, ViewGroup viewList, Map<T, CardViewHolder> map) {
        if (list == null || list.isEmpty()) {
            viewList.removeAllViews();
            map.clear();
            return;
        }
        if (viewList.getChildCount() == 0) {
            //add all folders to map
            for (T c : list) {
                View v = ViewUtils.inflate(getContext(), c.getLayout(), viewList, false);
                CardViewHolder vh = new CardViewHolder(v, mPresenter);
                vh.bind(c, this);
                map.put(c, vh);
            }
        } else {
            //clear old folders
            Iterator<Map.Entry<T, CardViewHolder>> iter =
                   map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<T, CardViewHolder> e = iter.next();
                if (!list.contains(e.getKey())) {
                    e.getValue().recycle();
                    iter.remove();
                }
            }
            //add new ones
            for (T c : list) {
                CardViewHolder vh = map.get(c);
                if (vh == null) {
                    View v = ViewUtils.inflate(getContext(), c.getLayout(), viewList, false);
                    vh = new CardViewHolder(v, mPresenter);
                    vh.bind(c, this);
                    map.put(c, vh);
                } else {
                    vh.recycle();
                    vh.bind(c, this);
                }
            }
        }
        //add or update the folders
        int ii=0;
        Iterator<T> itc = list.iterator();
        //we first loop through
        while (itc.hasNext() && ii<viewList.getChildCount()) {
            CardViewHolder vh = map.get(itc.next());
            //until we find a view out of order
            if (viewList.getChildAt(ii) != vh.itemView) {
                //remove all the remaining views
                for (int jj=viewList.getChildCount()-1; jj>=ii; jj--) {
                    viewList.removeViewAt(jj);
                }
                //add the ii'th view so we dont have to rewind iterator
                viewList.addView(vh.itemView);
                break;
            }
            ii++;
        }
        //then add all the remaining views
        while (itc.hasNext()) {
            CardViewHolder vh = map.get(itc.next());
            viewList.addView(vh.itemView);
        }
    }

    public void showSavingDialog() {
        showProgressDialog(getResources().getString(R.string.saving_config_dots));
    }

    public void dismissSavingDialog() {
        dismissProgressDialog();
    }

    public void showRestartDialog() {
        showProgressDialog(getResources().getString(R.string.syncthing_is_restarting));
    }

    public void dismissRestartDialog() {
        dismissProgressDialog();
    }

    public void showProgressDialog(String msg) {
        dismissErrorDialog();
        dismissProgressDialog();
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(msg);
        mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    public void showErrorDialog(String title, String msg) {
        dismissErrorDialog();
        dismissProgressDialog();
        mErrorDialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public void dismissErrorDialog() {
        if (mErrorDialog != null && mErrorDialog.isShowing()) {
            mErrorDialog.dismiss();
        }
    }

    public void showConfigSaved() {
        Toast.makeText(getContext(), R.string.config_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setListEmpty(boolean show, boolean animate) {
        mLoadingProgress.setVisibility(GONE);
        mEmptyView.setVisibility(show ? VISIBLE : GONE);
        mListContainer.setVisibility(show ? GONE : VISIBLE);
    }

    @Override
    public void setListShown(boolean show, boolean animate) {
        mListContainer.setVisibility(VISIBLE);
        mEmptyView.setVisibility(show ? GONE : VISIBLE);
        mLoadingProgress.setVisibility(GONE);
    }

    @Override
    public void setLoading(boolean loading) {
        mListContainer.setVisibility(VISIBLE);
        mEmptyView.setVisibility(GONE);
        mLoadingProgress.setVisibility(loading ? VISIBLE : GONE);
    }
}
