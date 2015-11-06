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

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.RadioGroup;

import com.jakewharton.rxbinding.widget.RxTextView;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import syncthing.api.model.SystemInfo;

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
        binding = DataBindingUtil.bind(this);
        binding.setPresenter(mPresenter);
        binding.radioGroupVersioning.setOnCheckedChangeListener(versioningChangeListener);
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        subscribeTextChanges();
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

    void subscribeTextChanges() {
        subscriptions = new CompositeSubscription(
                RxTextView.textChangeEvents(binding.editFolderId)
                        .subscribe(e -> onFolderIdChanged(e.text())),
                RxTextView.textChangeEvents(binding.editRescanInterval)
                        .subscribe(e -> onRescanIntrvlChanged(e.text())),
                RxTextView.textChangeEvents(binding.editTrashcanVersioningKeep)
                        .subscribe(e -> onTrashCanVerKeepChanged(e.text())),
                RxTextView.textChangeEvents(binding.editSimpleVersioningKeep)
                        .subscribe(e -> onSimpleVerKeepChanged(e.text())),
                RxTextView.textChangeEvents(binding.editStaggeredMaxAge)
                        .subscribe(e -> onStaggeredMaxAgeChange(e.text())),
                RxTextView.textChangeEvents(binding.editExternalVersioningCommand)
                        .subscribe(e -> onExternalVerCmdChange(e.text()))
        );
    }

    void initialize(List<DeviceConfig> devices, SystemInfo systemInfo, boolean fromsavedstate) {
        if (fromsavedstate) return;
        boolean isAdd = mPresenter.isAdd;
        FolderConfig folder = mPresenter.origFolder;
        if (!isAdd) {
            binding.editFolderId.setText(folder.id);
            binding.editFolderId.setEnabled(false);
            binding.descFolderId.setVisibility(GONE);
            binding.editFolderPath.setText(folder.path);
            binding.editFolderPath.setEnabled(false);
            binding.descFolderPath.setVisibility(GONE);
            binding.editRescanInterval.setText(String.valueOf(folder.rescanIntervalS));
            binding.checkFolderMaster.setChecked(folder.readOnly);
            binding.checkIgnorePermissions.setChecked(folder.ignorePerms);

            switch (folder.order) {
                case ALPHABETIC:
                    binding.radioGroupPullorder.check(R.id.radio_pullorder_alphabetic);
                    break;
                case SMALLESTFIRST:
                    binding.radioGroupPullorder.check(R.id.radio_pullorder_smallestfirst);
                    break;
                case LARGESTFIRST:
                    binding.radioGroupPullorder.check(R.id.radio_pullorder_largestfirst);
                    break;
                case OLDESTFIRST:
                    binding.radioGroupPullorder.check(R.id.radio_pullorder_oldestfirst);
                    break;
                case NEWESTFIRST:
                    binding.radioGroupPullorder.check(R.id.radio_pullorder_newestfirst);
                    break;
                case RANDOM:
                default:
                    binding.radioGroupPullorder.check(R.id.radio_pullorder_random);
                    break;
            }

            switch (folder.versioning.type) {
                case NONE:
                    binding.radioGroupVersioning.check(R.id.radio_no_versioning);
                    break;
                case TRASHCAN:
                    binding.radioGroupVersioning.check(R.id.radio_trashcan_versioning);
                    binding.editTrashcanVersioningKeep.setText(folder.versioning.params.cleanoutDays);
                    break;
                case SIMPLE:
                    binding.radioGroupVersioning.check(R.id.radio_simple_versioning);
                    binding.editSimpleVersioningKeep.setText(folder.versioning.params.keep);
                    break;
                case STAGGERED:
                    binding.radioGroupVersioning.check(R.id.radio_staggered_versioning);
                    binding.editStaggeredMaxAge.setText(SyncthingUtils.secondsToDays(folder.versioning.params.maxAge));
                    binding.editStaggeredPath.setText(folder.versioning.params.versionPath);
                    break;
                case EXTERNAL:
                    binding.radioGroupVersioning.check(R.id.radio_external_versioning);
                    binding.editExternalVersioningCommand.setText(folder.versioning.params.command);
                    break;
            }

            binding.addWarning.setVisibility(GONE);
        } else {
            //Initialize with nice defaults
            if (!StringUtils.isEmpty(folder.id)) {
                //when adding new share folder id will already be set
                binding.editFolderId.setText(folder.id);
                binding.editFolderId.setEnabled(false);
                binding.descFolderId.setVisibility(GONE);
                binding.addWarning.setVisibility(GONE);
            } else {
                binding.addWarning.setVisibility(VISIBLE);
            }
            binding.editRescanInterval.setText(String.valueOf(folder.rescanIntervalS));
            binding.editFolderPath.setText(systemInfo.tilde);
            binding.editFolderPath.setAdapter(editFolderPathAdapter);
            //TODO set ignore perms if running on android
            binding.radioGroupPullorder.check(R.id.radio_pullorder_random);
            binding.radioGroupVersioning.check(R.id.radio_no_versioning);
            binding.editSimpleVersioningKeep.setText(folder.versioning.params.keep);
            binding.editStaggeredMaxAge.setText(SyncthingUtils.secondsToDays(folder.versioning.params.maxAge));
        }

        binding.descFolderPath.setText(binding.descFolderPath.getText() + " " + systemInfo.tilde);//TODO Hacky

        binding.shareDevicesContainer.removeAllViews();
        for (DeviceConfig device : devices) {
            CheckBox checkBox = new DeviceCheckBox(getContext(), device);
            checkBox.setText(SyncthingUtils.getDisplayName(device));
            for (FolderDeviceConfig d : folder.devices) {
                if (StringUtils.equals(d.deviceID, device.deviceID)) {
                    checkBox.setChecked(true);
                    break;
                }
            }
            binding.shareDevicesContainer.addView(checkBox);
        }

        binding.btnDelete.setVisibility(isAdd ? GONE : VISIBLE);
        binding.btnIgnorePtrn.setVisibility(isAdd ? GONE : VISIBLE);

    }

    void onFolderIdChanged(CharSequence text) {
        boolean isAdd = mPresenter.isAdd;
        if (isAdd && !StringUtils.isEmpty(text)) {
            mPresenter.validateFolderId(text.toString());
        }
    }

    void notifyEmptyFolderId(boolean valid) {
        binding.descFolderId.setVisibility(valid ? VISIBLE : GONE );
        binding.errorFolderIdBlank.setVisibility(valid ? GONE : VISIBLE);
        binding.errorFolderIdUnique.setVisibility(GONE);
        binding.errorFolderIdInvalid.setVisibility(GONE);
    }

    void notifyInvalidFolderId(boolean valid) {
        binding.descFolderId.setVisibility(valid ? VISIBLE : GONE);
        binding.errorFolderIdBlank.setVisibility(GONE);
        binding.errorFolderIdUnique.setVisibility(GONE);
        binding.errorFolderIdInvalid.setVisibility(valid ? GONE : VISIBLE);
    }

    void notifyNotUniqueFolderId(boolean valid) {
        binding.descFolderId.setVisibility(valid ? VISIBLE : GONE);
        binding.errorFolderIdBlank.setVisibility(GONE);
        binding.errorFolderIdUnique.setVisibility(valid ? GONE : VISIBLE);
        binding.errorFolderIdInvalid.setVisibility(GONE);
    }

    void notifyEmptyFolderPath(boolean valid) {
        binding.descFolderPath.setVisibility(valid ? VISIBLE : GONE);
        binding.errorFolderPathBlank.setVisibility(valid ? GONE : VISIBLE);
    }

    void onRescanIntrvlChanged(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            mPresenter.validateRescanInterval(text.toString());
        }
    }

    void notifyInvalidRescanInterval(boolean valid) {
        binding.errorRescanInterval.setVisibility(valid ? GONE : VISIBLE);
    }

    void onTrashCanVerKeepChanged(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            mPresenter.validateTrashCanVersioningKeep(text.toString());
        }
    }

    void notifyTrashCanVersioningKeepInvalid(boolean valid) {
        binding.descTrashcanVersioning.setVisibility(valid ? VISIBLE : GONE);
        binding.errorTrashcanVersioning.setVisibility(valid ? GONE : VISIBLE);
    }

    void onSimpleVerKeepChanged(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            mPresenter.validateSimpleVersioningKeep(text.toString());
        }
    }

    void notifySimpleVersioningKeepEmpty(boolean valid) {
        binding.descSimpleVersioningKeep.setVisibility(valid ? VISIBLE : GONE);
        binding.errorSimpleVersioningKeepBlank.setVisibility(valid ? GONE : VISIBLE);
        binding.errorSimpleVersioningKeepInvalid.setVisibility(GONE);
    }

    void notifySimpleVersioningKeepInvalid(boolean valid) {
        binding.descSimpleVersioningKeep.setVisibility(valid ? VISIBLE : GONE);
        binding.errorSimpleVersioningKeepBlank.setVisibility(GONE);
        binding.errorSimpleVersioningKeepInvalid.setVisibility(valid ? GONE : VISIBLE);
    }

    void onStaggeredMaxAgeChange(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            mPresenter.validateStaggeredMaxAge(text.toString());
        }
    }

    void notifyStaggeredMaxAgeInvalid(boolean valid) {
        binding.descStaggeredMaxAge.setVisibility(valid ? VISIBLE : GONE);
        binding.errorStaggeredMaxAgeInvalid.setVisibility(valid ? GONE : VISIBLE);
    }

    void onExternalVerCmdChange(CharSequence text) {
        mPresenter.validateExternalVersioningCmd(text.toString());
    }

    void notifyExternalVersioningCmdInvalid(boolean valid) {
        binding.descExternalVersioningCommand.setVisibility(valid ? VISIBLE : GONE);
        binding.errorExternalVersioningCommandBlank.setVisibility(valid ? GONE : VISIBLE);
    }

    final RadioGroup.OnCheckedChangeListener versioningChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_trashcan_versioning:
                    binding.trashcanVersioningExtra.setVisibility(VISIBLE);
                    binding.simpleVersioningExtra.setVisibility(GONE);
                    binding.staggeredVersioningExtra.setVisibility(GONE);
                    binding.externalVersioningExtra.setVisibility(GONE);
                    break;
                case R.id.radio_simple_versioning:
                    binding.trashcanVersioningExtra.setVisibility(GONE);
                    binding.simpleVersioningExtra.setVisibility(VISIBLE);
                    binding.staggeredVersioningExtra.setVisibility(GONE);
                    binding.externalVersioningExtra.setVisibility(GONE);
                    break;
                case R.id.radio_staggered_versioning:
                    binding.trashcanVersioningExtra.setVisibility(GONE);
                    binding.simpleVersioningExtra.setVisibility(GONE);
                    binding.staggeredVersioningExtra.setVisibility(VISIBLE);
                    binding.externalVersioningExtra.setVisibility(GONE);
                    break;
                case R.id.radio_external_versioning:
                    binding.trashcanVersioningExtra.setVisibility(GONE);
                    binding.simpleVersioningExtra.setVisibility(GONE);
                    binding.staggeredVersioningExtra.setVisibility(GONE);
                    binding.externalVersioningExtra.setVisibility(VISIBLE);
                    break;
                case R.id.radio_no_versioning:
                default:
                    binding.trashcanVersioningExtra.setVisibility(GONE);
                    binding.simpleVersioningExtra.setVisibility(GONE);
                    binding.staggeredVersioningExtra.setVisibility(GONE);
                    binding.externalVersioningExtra.setVisibility(GONE);
                    break;
            }
        }
    };

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

    @SuppressLint("ViewConstructor")
    static class DeviceCheckBox extends CheckBox {
        final DeviceConfig device;
        DeviceCheckBox(Context context, DeviceConfig device) {
            super(context);
            this.device = device;
        }
    }
}
