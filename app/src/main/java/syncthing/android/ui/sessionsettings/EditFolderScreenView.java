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

package syncthing.android.ui.sessionsettings;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxRadioGroup;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.PullOrder;
import syncthing.api.model.VersioningType;

/**
 * Created by drew on 3/16/15.
 */
public class EditFolderScreenView extends CoordinatorLayout {

    @Inject ToolbarOwner mToolbarOwner;
    @Inject EditFolderPresenter mPresenter;
    final DirectoryAutoCompleteAdapter editFolderPathAdapter;
    CompositeSubscription subscriptions;
    syncthing.android.ui.sessionsettings.EditFolderScreenViewBinding binding;

    public EditFolderScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        editFolderPathAdapter = new DirectoryAutoCompleteAdapter(getContext());
        if (!isInEditMode()) {
            EditFolderComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            binding = DataBindingUtil.bind(this);
            mPresenter.takeView(this);
            binding.setPresenter(mPresenter);
            binding.editFolderPath.setAdapter(editFolderPathAdapter);
//            binding.descFolderPath.setText(binding.descFolderPath.getText() + " " + systemInfo.tilde);//TODO Hacky
            binding.executePendingBindings();
            subscribeChanges();
            addShareDevices();
        }
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mToolbarOwner.attachToolbar(binding.toolbar);
            mToolbarOwner.setConfig(mPresenter.getToolbarConfig());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        mToolbarOwner.detachToolbar(binding.toolbar);
        if (subscriptions != null) subscriptions.unsubscribe();
    }

    void subscribeChanges() {
        subscriptions = new CompositeSubscription(
                RxTextView.textChanges(binding.editFolderId)
                        .subscribe(mPresenter::setFolderId),
                RxTextView.textChanges(binding.editFolderPath)
                        .subscribe(mPresenter::setFolderPath),
                RxTextView.textChanges(binding.editRescanInterval)
                        .subscribe(mPresenter::setRescanInterval),
                RxCompoundButton.checkedChanges(binding.checkFolderMaster)
                        .subscribe(mPresenter::setReadOnly),
                RxCompoundButton.checkedChanges(binding.checkIgnorePermissions)
                        .subscribe(mPresenter::setIgnorePerms),
                RxRadioGroup.checkedChanges(binding.radioGroupPullorder)
                        .subscribe(this::onPullOrderCheckedChanged),
                RxRadioGroup.checkedChanges(binding.radioGroupVersioning)
                        .subscribe(this::onVersioningCheckedChanged),
                RxTextView.textChanges(binding.editTrashcanVersioningKeep)
                        .subscribe(mPresenter::setTrashCanParamCleanDays),
                RxTextView.textChanges(binding.editSimpleVersioningKeep)
                        .subscribe(mPresenter::setSimpleParamKeep),
                RxTextView.textChanges(binding.editStaggeredMaxAge)
                        .subscribe(mPresenter::setStaggeredParamMaxAge),
                RxTextView.textChanges(binding.editStaggeredPath)
                        .subscribe(mPresenter::setStaggeredParamPath),
                RxTextView.textChanges(binding.editExternalVersioningCmd)
                        .subscribe(mPresenter::setExternalParamCmd)
        );
    }

    void addShareDevices() {
        binding.shareDevicesContainer.removeAllViews();
        for (Map.Entry<String, Boolean> e : mPresenter.sharedDevices.entrySet()) {
            final String id = e.getKey();
            CheckBox checkBox = new CheckBox(getContext());
            DeviceConfig device = mPresenter.controller.getDevice(id);
            if (device == null) {
                device = new DeviceConfig();
                device.deviceID = id;
            }
            checkBox.setText(SyncthingUtils.getDisplayName(device));
            checkBox.setChecked(e.getValue());
            subscriptions.add(RxCompoundButton.checkedChanges(checkBox)
                    .subscribe(b -> {
                        mPresenter.setDeviceShared(id, b);
                    }));
            binding.shareDevicesContainer.addView(checkBox);
        }
    }

    void onPullOrderCheckedChanged(int checkedId) {
        switch (checkedId) {
            case R.id.radio_pullorder_alphabetic:
                mPresenter.setPullOrder(PullOrder.ALPHABETIC);
                break;
            case R.id.radio_pullorder_smallestfirst:
                mPresenter.setPullOrder(PullOrder.SMALLESTFIRST);
                break;
            case R.id.radio_pullorder_largestfirst:
                mPresenter.setPullOrder(PullOrder.LARGESTFIRST);
                break;
            case R.id.radio_pullorder_oldestfirst:
                mPresenter.setPullOrder(PullOrder.OLDESTFIRST);
                break;
            case R.id.radio_pullorder_newestfirst:
                mPresenter.setPullOrder(PullOrder.NEWESTFIRST);
                break;
            case R.id.radio_pullorder_random:
                mPresenter.setPullOrder(PullOrder.RANDOM);
                break;
        }
    }

    void onVersioningCheckedChanged(int checkedId) {
        switch (checkedId) {
            case R.id.radio_trashcan_versioning:
                mPresenter.setVersioningType(VersioningType.TRASHCAN);
                break;
            case R.id.radio_simple_versioning:
                mPresenter.setVersioningType(VersioningType.SIMPLE);
                break;
            case R.id.radio_staggered_versioning:
                mPresenter.setVersioningType(VersioningType.STAGGERED);
                break;
            case R.id.radio_external_versioning:
                mPresenter.setVersioningType(VersioningType.EXTERNAL);
                break;
            case R.id.radio_no_versioning:
                mPresenter.setVersioningType(VersioningType.NONE);
                break;
        }
    }

    class DirectoryAutoCompleteAdapter extends ArrayAdapter<String> {
        DirectoryAutoCompleteAdapter(Context context) {
            super(context, android.R.layout.simple_dropdown_item_1line);
            setNotifyOnChange(false);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    try {
                        List<String> results = mPresenter.controller
                                .getAutoCompleteDirectoryList(constraint.toString())
                                .toBlocking().first();
                        FilterResults fr = new FilterResults();
                        fr.values = results;
                        fr.count = results.size();
                        return fr;
                    } catch (Exception e) { //cant remember what in throws
                        FilterResults fr = new FilterResults();
                        fr.values = new ArrayList<String>();
                        fr.count = 0;
                        return fr;
                    }
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                        if (results.count == 0) {
                            clear();
                            notifyDataSetInvalidated();
                        } else {
                            clear();
                            addAll((List<String>)results.values);
                            notifyDataSetChanged();
                        }
                }
            };
        }
    }

}
