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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import syncthing.api.model.PullOrder;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.VersioningConfig;
import syncthing.api.model.VersioningType;

/**
 * Created by drew on 3/16/15.
 */
public class EditFolderScreenView extends ScrollView {

    @InjectView(R.id.edit_folder_id) EditText editFolderId;
    @InjectView(R.id.desc_folder_id) TextView descFolderId;
    @InjectView(R.id.error_folder_id_unique) TextView errorFolderIdUnique;
    @InjectView(R.id.error_folder_id_blank) TextView errorFolderIdBlank;
    @InjectView(R.id.error_folder_id_invalid) TextView errorFolderIdInvalid;
    @InjectView(R.id.edit_folder_path) AutoCompleteTextView editFolderPath;
    @InjectView(R.id.desc_folder_path) TextView descFolderPath;
    @InjectView(R.id.error_folder_path_blank) TextView errorFolderPathBlank;
    @InjectView(R.id.edit_rescan_interval) EditText editRescanIntrvl;
    @InjectView(R.id.error_rescan_interval) TextView errorRescanIntrvl;
    @InjectView(R.id.check_folder_master) CheckBox checkFolderMaster;
    @InjectView(R.id.check_ignore_permissions) CheckBox checkIgnorePerms;
    @InjectView(R.id.radio_group_pullorder) RadioGroup pullOrderGroup;
    @InjectView(R.id.radio_group_versioning) RadioGroup rdioVerGroup;
    @InjectView(R.id.radio_no_versioning) RadioButton rdioNoVer;
    @InjectView(R.id.radio_simple_versioning) RadioButton rdioSimpleVer;
    @InjectView(R.id.radio_staggered_versioning) RadioButton rdioStaggeredVer;
    @InjectView(R.id.simple_versioning_extra) ViewGroup simpleVerExtra;
    @InjectView(R.id.edit_simple_versioning_keep) EditText editSimpleVerKeep;
    @InjectView(R.id.desc_simple_versioning_keep) TextView descSimpleVerKeep;
    @InjectView(R.id.error_simple_versioning_keep_blank) TextView errorSimpleVerKeepBlank;
    @InjectView(R.id.error_simple_versioning_keep_invalid) TextView errorSimpleVerKeepInvalid;
    @InjectView(R.id.staggered_versioning_extra) ViewGroup staggeredVerExtra;
    @InjectView(R.id.edit_staggered_max_age) EditText editStaggeredVerMaxAge;
    @InjectView(R.id.desc_staggered_max_age) TextView descStaggeredVerMaxAge;
    @InjectView(R.id.error_staggered_max_age_invalid) TextView errorStaggeredMaxAgeInvalid;
    @InjectView(R.id.edit_staggered_path) EditText editStaggeredVerPath;
    @InjectView(R.id.radio_external_versioning) RadioButton rdioExternalVer;
    @InjectView(R.id.external_versioning_extra) ViewGroup externalVerExtra;
    @InjectView(R.id.edit_external_versioning_command) EditText editExternalVerCmd;
    @InjectView(R.id.desc_external_versioning_command) TextView descExternalVerCmd;
    @InjectView(R.id.error_external_versioning_command_blank) TextView errorExternalVerCmdBlank;
    @InjectView(R.id.share_devices_container) ViewGroup sharedDevicesContainer;
    @InjectView(R.id.add_warning) View addingWarning;
    @InjectView(R.id.btn_delete) Button deleteBtn;
    @InjectView(R.id.btn_ignore_ptrn) Button ignoresPattrnBtn;

    final EditFolderPresenter presenter;
    final DirectoryAutoCompleteAdapter editFolderPathAdapter;

    boolean isAdd = false;
    FolderConfig folder;

    public EditFolderScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<EditFolderComponent>getDaggerComponent(getContext()).presenter();
        editFolderPathAdapter = new DirectoryAutoCompleteAdapter(getContext());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        rdioVerGroup.setOnCheckedChangeListener(versioningChangeListener);
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
    }

    @OnClick(R.id.btn_delete)
    void doDelete() {
        presenter.deleteFolder();
    }

    @OnClick(R.id.btn_ignore_ptrn)
    void openIgnoresEditor() {
        presenter.openIgnoresEditor();
    }

    @OnClick(R.id.btn_cancel)
    void doCancel() {
        presenter.dismissDialog();
    }

    @OnClick(R.id.btn_save)
    void doSave() {
        if (isAdd && !presenter.validateFolderId(editFolderId.getText().toString())) {
            return;
        }
        folder.id = editFolderId.getText().toString();
        if (isAdd && !presenter.validateFolderPath(editFolderPath.getText().toString())) {
            return;
        }
        folder.path = editFolderPath.getText().toString();
        if (!presenter.validateRescanInterval(editRescanIntrvl.getText().toString())) {
            return;
        }
        folder.rescanIntervalS = Integer.decode(editRescanIntrvl.getText().toString());
        folder.readOnly = checkFolderMaster.isChecked();
        folder.ignorePerms = checkIgnorePerms.isChecked();

        switch (pullOrderGroup.getCheckedRadioButtonId()) {
            case R.id.radio_pullorder_alphabetic:
                folder.order = PullOrder.ALPHABETIC;
                break;
            case R.id.radio_pullorder_smallestfirst:
                folder.order = PullOrder.SMALLESTFIRST;
                break;
            case R.id.radio_pullorder_largestfirst:
                folder.order = PullOrder.LARGESTFIRST;
                break;
            case R.id.radio_pullorder_oldestfirst:
                folder.order = PullOrder.OLDESTFIRST;
                break;
            case R.id.radio_pullorder_newestfirst:
                folder.order = PullOrder.NEWESTFIRST;
                break;
            case R.id.radio_pullorder_random:
            default:
                folder.order = PullOrder.RANDOM;
                break;
        }

        switch (rdioVerGroup.getCheckedRadioButtonId()) {
            case R.id.radio_simple_versioning:
                folder.versioning = new VersioningConfig();
                folder.versioning.type = VersioningType.SIMPLE;
                if (!presenter.validateSimpleVersioningKeep(editSimpleVerKeep.getText().toString())) {
                    return;
                }
                folder.versioning.params.keep = editSimpleVerKeep.getText().toString();
                break;
            case R.id.radio_staggered_versioning:
                folder.versioning = new VersioningConfig();
                folder.versioning.type = VersioningType.STAGGERED;
                if (!presenter.validateStaggeredMaxAge(editStaggeredVerMaxAge.getText().toString())) {
                    return;
                }
                folder.versioning.params.maxAge = SyncthingUtils.daysToSeconds(editStaggeredVerMaxAge.getText().toString());
                if (!StringUtils.isEmpty(editStaggeredVerPath.getText().toString())) {
                    folder.versioning.params.versionPath = editStaggeredVerPath.getText().toString();
                }
                break;
            case R.id.radio_external_versioning:
                folder.versioning = new VersioningConfig();
                folder.versioning.type = VersioningType.EXTERNAL;
                if (!presenter.validateExternalVersioningCmd(editExternalVerCmd.getText().toString())) {
                    return;
                }
                folder.versioning.params.command = editExternalVerCmd.getText().toString();
                break;
            case R.id.radio_no_versioning:
            default:
                folder.versioning = new VersioningConfig();
                break;
        }

        List<FolderDeviceConfig> devices = new ArrayList<>();
        for (int ii=0; ii< sharedDevicesContainer.getChildCount(); ii++) {
            View v = sharedDevicesContainer.getChildAt(ii);
            if (v instanceof DeviceCheckBox) {
                DeviceCheckBox cb = (DeviceCheckBox) v;
                if (cb.isChecked()) {
                    devices.add(new FolderDeviceConfig(cb.device.deviceID));
                }
            }
        }
        folder.devices = devices;

        presenter.saveFolder();

    }

    void initialize(boolean isAdd, FolderConfig folder, List<DeviceConfig> devices, SystemInfo systemInfo, boolean fromsavedstate) {
        this.isAdd = isAdd;
        this.folder = folder;
        if (fromsavedstate) return;

        if (!isAdd) {
            editFolderId.setText(folder.id);
            editFolderId.setEnabled(false);
            descFolderId.setVisibility(GONE);
            editFolderPath.setText(folder.path);
            editFolderPath.setEnabled(false);
            descFolderPath.setVisibility(GONE);
            editRescanIntrvl.setText(String.valueOf(folder.rescanIntervalS));
            checkFolderMaster.setChecked(folder.readOnly);
            checkIgnorePerms.setChecked(folder.ignorePerms);

            switch (folder.order) {
                case ALPHABETIC:
                    pullOrderGroup.check(R.id.radio_pullorder_alphabetic);
                    break;
                case SMALLESTFIRST:
                    pullOrderGroup.check(R.id.radio_pullorder_smallestfirst);
                    break;
                case LARGESTFIRST:
                    pullOrderGroup.check(R.id.radio_pullorder_largestfirst);
                    break;
                case OLDESTFIRST:
                    pullOrderGroup.check(R.id.radio_pullorder_oldestfirst);
                    break;
                case NEWESTFIRST:
                    pullOrderGroup.check(R.id.radio_pullorder_newestfirst);
                    break;
                case RANDOM:
                default:
                    pullOrderGroup.check(R.id.radio_pullorder_random);
                    break;
            }

            switch (folder.versioning.type) {
                case NONE:
                    rdioVerGroup.check(R.id.radio_no_versioning);
                    break;
                case SIMPLE:
                    rdioVerGroup.check(R.id.radio_simple_versioning);
                    editSimpleVerKeep.setText(folder.versioning.params.keep);
                    break;
                case STAGGERED:
                    rdioVerGroup.check(R.id.radio_staggered_versioning);
                    editStaggeredVerMaxAge.setText(SyncthingUtils.secondsToDays(folder.versioning.params.maxAge));
                    editStaggeredVerPath.setText(folder.versioning.params.versionPath);
                    break;
                case EXTERNAL:
                    rdioVerGroup.check(R.id.radio_external_versioning);
                    editExternalVerCmd.setText(folder.versioning.params.command);
                    break;
            }

            addingWarning.setVisibility(GONE);
        } else {
            //Initialize with nice defaults
            if (!StringUtils.isEmpty(folder.id)) {
                //when adding new share folder id will already be set
                editFolderId.setText(folder.id);
                editFolderId.setEnabled(false);
                descFolderId.setVisibility(GONE);
                addingWarning.setVisibility(GONE);
            } else {
                addingWarning.setVisibility(VISIBLE);
            }
            editRescanIntrvl.setText(String.valueOf(folder.rescanIntervalS));
            editFolderPath.setText(systemInfo.tilde);
            editFolderPath.setAdapter(editFolderPathAdapter);
            //TODO set ignore perms if running on android
            pullOrderGroup.check(R.id.radio_pullorder_random);
            rdioVerGroup.check(R.id.radio_no_versioning);
            editSimpleVerKeep.setText(folder.versioning.params.keep);
            editStaggeredVerMaxAge.setText(SyncthingUtils.secondsToDays(folder.versioning.params.maxAge));
        }

        descFolderPath.setText(descFolderPath.getText() + " " + systemInfo.tilde);//TODO Hacky

        sharedDevicesContainer.removeAllViews();
        for (DeviceConfig device : devices) {
            CheckBox checkBox = new DeviceCheckBox(getContext(), device);
            checkBox.setText(SyncthingUtils.getDisplayName(device));
            for (FolderDeviceConfig d : folder.devices) {
                if (StringUtils.equals(d.deviceID, device.deviceID)) {
                    checkBox.setChecked(true);
                    break;
                }
            }
            sharedDevicesContainer.addView(checkBox);
        }

        deleteBtn.setVisibility(isAdd ? GONE : VISIBLE);
        ignoresPattrnBtn.setVisibility(isAdd ? GONE : VISIBLE);

    }

    @OnTextChanged(R.id.edit_folder_id)
    void onFolderIdChanged(CharSequence text) {
        if (isAdd && !StringUtils.isEmpty(text)) {
            presenter.validateFolderId(text.toString());
        }
    }

    void notifyEmptyFolderId(boolean valid) {
        descFolderId.setVisibility(valid ? VISIBLE : GONE );
        errorFolderIdBlank.setVisibility(valid ? GONE : VISIBLE);
        errorFolderIdUnique.setVisibility(GONE);
        errorFolderIdInvalid.setVisibility(GONE);
    }

    void notifyInvalidFolderId(boolean valid) {
        descFolderId.setVisibility(valid ? VISIBLE : GONE);
        errorFolderIdBlank.setVisibility(GONE);
        errorFolderIdUnique.setVisibility(GONE);
        errorFolderIdInvalid.setVisibility(valid ? GONE : VISIBLE);
    }

    void notifyNotUniqueFolderId(boolean valid) {
        descFolderId.setVisibility(valid ? VISIBLE : GONE);
        errorFolderIdBlank.setVisibility(GONE);
        errorFolderIdUnique.setVisibility(valid ? GONE : VISIBLE);
        errorFolderIdInvalid.setVisibility(GONE);
    }

    void notifyEmptyFolderPath(boolean valid) {
        descFolderPath.setVisibility(valid ? VISIBLE : GONE);
        errorFolderPathBlank.setVisibility(valid ? GONE : VISIBLE);
    }

    @OnTextChanged(R.id.edit_rescan_interval)
    void onRescanIntrvlChanged(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            presenter.validateRescanInterval(text.toString());
        }
    }

    void notifyInvalidRescanInterval(boolean valid) {
        errorRescanIntrvl.setVisibility(valid ? GONE : VISIBLE);
    }

    @OnTextChanged(R.id.edit_simple_versioning_keep)
    void onSimpleVerKeepChanged(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            presenter.validateSimpleVersioningKeep(text.toString());
        }
    }

    void notifySimpleVersioningKeepEmpty(boolean valid) {
        descSimpleVerKeep.setVisibility(valid ? VISIBLE : GONE);
        errorSimpleVerKeepBlank.setVisibility(valid ? GONE : VISIBLE);
        errorSimpleVerKeepInvalid.setVisibility(GONE);
    }

    void notifySimpleVersioningKeepInvalid(boolean valid) {
        descSimpleVerKeep.setVisibility(valid ? VISIBLE : GONE);
        errorSimpleVerKeepBlank.setVisibility(GONE);
        errorSimpleVerKeepInvalid.setVisibility(valid ? GONE : VISIBLE);
    }

    @OnTextChanged(R.id.edit_staggered_max_age)
    void onStaggeredMaxAgeChange(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            presenter.validateStaggeredMaxAge(text.toString());
        }
    }

    void notifyStaggeredMaxAgeInvalid(boolean valid) {
        descStaggeredVerMaxAge.setVisibility(valid ? VISIBLE : GONE);
        errorStaggeredMaxAgeInvalid.setVisibility(valid ? GONE : VISIBLE);
    }

    @OnTextChanged(R.id.edit_external_versioning_command)
    void onExternalVerCmdChange(CharSequence text) {
        presenter.validateExternalVersioningCmd(text.toString());
    }

    void notifyExternalVersioningCmdInvalid(boolean valid) {
        descExternalVerCmd.setVisibility(valid ? VISIBLE : GONE);
        errorExternalVerCmdBlank.setVisibility(valid ? GONE : VISIBLE);
    }

    final RadioGroup.OnCheckedChangeListener versioningChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_simple_versioning:
                    simpleVerExtra.setVisibility(VISIBLE);
                    staggeredVerExtra.setVisibility(GONE);
                    externalVerExtra.setVisibility(GONE);
                    break;
                case R.id.radio_staggered_versioning:
                    simpleVerExtra.setVisibility(GONE);
                    staggeredVerExtra.setVisibility(VISIBLE);
                    externalVerExtra.setVisibility(GONE);
                    break;
                case R.id.radio_external_versioning:
                    simpleVerExtra.setVisibility(GONE);
                    staggeredVerExtra.setVisibility(GONE);
                    externalVerExtra.setVisibility(VISIBLE);
                    break;
                case R.id.radio_no_versioning:
                default:
                    simpleVerExtra.setVisibility(GONE);
                    staggeredVerExtra.setVisibility(GONE);
                    externalVerExtra.setVisibility(GONE);
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
                        List<String> results = presenter.controller
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
