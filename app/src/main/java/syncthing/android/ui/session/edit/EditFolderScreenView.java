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

package syncthing.android.ui.session.edit;

import android.app.AlertDialog;
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
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import mortar.dagger2support.DaggerService;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
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
    @InjectView(R.id.share_devices_container) ViewGroup sharedDevicesContainer;
    @InjectView(R.id.add_warning) View addingWarning;
    @InjectView(R.id.btn_delete) Button deleteBtn;
    @InjectView(R.id.btn_ignore_ptrn) Button ignoresPattrnBtn;

    final EditFolderPresenter presenter;
    final DirectoryAutoCompleteAdapter editFolderPathAdapter;

    boolean isAdd = false;
    AlertDialog errorDialog;


    public EditFolderScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            presenter = null;
        } else {
            presenter = DaggerService.<EditFolderComponent>getDaggerComponent(getContext()).presenter();
        }
        editFolderPathAdapter = new DirectoryAutoCompleteAdapter(getContext());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        rdioVerGroup.setOnCheckedChangeListener(versioningChangeListener);
        if(!isInEditMode()){
            presenter.takeView(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
        dismissErrorDialog();
    }

    @OnClick(R.id.btn_delete)
    void doDelete() {
        presenter.deleteFolder();
    }

    @OnClick(R.id.btn_ignore_ptrn)
    void openIgnoresEditor() {

    }

    @OnClick(R.id.btn_cancel)
    void doCancel() {
        presenter.dismissDialog();
    }

    @OnClick(R.id.btn_save)
    void doSave() {
        FolderConfig folder = new FolderConfig();
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

        presenter.saveFolder(folder);

    }

    void initialize(boolean isAdd, FolderConfig folder, DeviceConfig shareDevice, List<DeviceConfig> devices, SystemInfo systemInfo) {
        this.isAdd = isAdd;

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

            switch (folder.versioning.type) {
                case NONE:
                    rdioNoVer.setChecked(true);
                    break;
                case SIMPLE:
                    rdioSimpleVer.setChecked(true);
                    editSimpleVerKeep.setText(folder.versioning.params.keep);
                    break;
                case STAGGERED:
                    rdioStaggeredVer.setChecked(true);
                    editStaggeredVerMaxAge.setText(SyncthingUtils.secondsToDays(folder.versioning.params.maxAge));
                    editStaggeredVerPath.setText(folder.versioning.params.versionPath);
                    break;
            }

        } else {
            //Initialize with nice defaults
            FolderConfig nf = FolderConfig.withDefaults();
            editRescanIntrvl.setText(String.valueOf(nf.rescanIntervalS));
            editFolderPath.setText(systemInfo.tilde);
            editFolderPath.setAdapter(editFolderPathAdapter);
            //TODO set ignore perms if running on android
            rdioNoVer.setChecked(true);
            editSimpleVerKeep.setText(nf.versioning.params.keep);
            editStaggeredVerMaxAge.setText(SyncthingUtils.secondsToDays(nf.versioning.params.maxAge));
        }

        descFolderPath.setText(descFolderPath.getText() + " " + systemInfo.tilde);//Hacky

        sharedDevicesContainer.removeAllViews();
        for (DeviceConfig device : devices) {
            CheckBox checkBox = new DeviceCheckBox(getContext(), device);
            checkBox.setText(SyncthingUtils.getDisplayName(device));
            if (!isAdd) {
                for (FolderDeviceConfig d : folder.devices) {
                    if (StringUtils.equals(d.deviceID, device.deviceID)) {
                        checkBox.setChecked(true);
                        break;
                    }
                }
                if (shareDevice != null) {
                    if (StringUtils.equals(shareDevice.deviceID, device.deviceID)) {
                        checkBox.setChecked(true);
                    }
                }
            }
            sharedDevicesContainer.addView(checkBox);
        }

        addingWarning.setVisibility(isAdd ? VISIBLE : GONE);
        deleteBtn.setVisibility(isAdd ? GONE : VISIBLE);
        ignoresPattrnBtn.setVisibility(isAdd ? GONE : VISIBLE);

    }

    @OnTextChanged(R.id.edit_folder_id)
    void onFolderIdChanged(CharSequence text) {
        if (isAdd && !StringUtils.isEmpty(text)) {
            if (presenter.validateFolderId(text)) {
                descFolderId.setVisibility(VISIBLE);
                errorFolderIdBlank.setVisibility(GONE);
                errorFolderIdUnique.setVisibility(GONE);
                errorFolderIdInvalid.setVisibility(GONE);
            }
        }
    }

    void notifyEmptyFolderId() {
        descFolderId.setVisibility(GONE);
        errorFolderIdBlank.setVisibility(VISIBLE);
        errorFolderIdUnique.setVisibility(GONE);
        errorFolderIdInvalid.setVisibility(GONE);
    }

    void notifyInvalidFolderId() {
        descFolderId.setVisibility(GONE);
        errorFolderIdBlank.setVisibility(GONE);
        errorFolderIdUnique.setVisibility(GONE);
        errorFolderIdInvalid.setVisibility(VISIBLE);
    }

    void notifyNotUniqueFolderId() {
        descFolderId.setVisibility(GONE);
        errorFolderIdBlank.setVisibility(GONE);
        errorFolderIdUnique.setVisibility(VISIBLE);
        errorFolderIdInvalid.setVisibility(GONE);
    }

    void notifyEmptyFolderPath() {
        descFolderPath.setVisibility(GONE);
        errorFolderPathBlank.setVisibility(VISIBLE);
    }

    @OnTextChanged(R.id.edit_rescan_interval)
    void onRescanIntrvlChanged(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            if (presenter.validateRescanInterval(text)) {
                errorRescanIntrvl.setVisibility(GONE);
            }
        }
    }

    void notifyInvalidRescanInterval() {
        errorRescanIntrvl.setVisibility(VISIBLE);
    }

    @OnTextChanged(R.id.edit_simple_versioning_keep)
    void onSimpleVerKeepChanged(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            if (presenter.validateSimpleVersioningKeep(text)) {
                descSimpleVerKeep.setVisibility(VISIBLE);
                errorSimpleVerKeepBlank.setVisibility(GONE);
                errorSimpleVerKeepInvalid.setVisibility(GONE);
            }
        }
    }

    void notifySimpleVersioningKeepEmpty() {
        descSimpleVerKeep.setVisibility(GONE);
        errorSimpleVerKeepBlank.setVisibility(VISIBLE);
        errorSimpleVerKeepInvalid.setVisibility(GONE);
    }

    void notifySimpleVersioningKeepInvalid() {
        descSimpleVerKeep.setVisibility(GONE);
        errorSimpleVerKeepBlank.setVisibility(GONE);
        errorSimpleVerKeepInvalid.setVisibility(VISIBLE);
    }

    @OnTextChanged(R.id.edit_staggered_max_age)
    void onStaggeredMaxAgeChange(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            presenter.validateStaggeredMaxAge(text);
        }
    }

    void notifyStaggeredMaxAgeInvalid() {
        descStaggeredVerMaxAge.setVisibility(GONE);
        errorStaggeredMaxAgeInvalid.setVisibility(GONE);
    }

    void showError(String msg) {
        dismissErrorDialog();
        errorDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.error)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    void dismissErrorDialog() {
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
        }
    }

    void showConfigSaved() {
        Toast.makeText(getContext(), R.string.config_saved, Toast.LENGTH_SHORT).show();
    }

    final RadioGroup.OnCheckedChangeListener versioningChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_simple_versioning:
                    simpleVerExtra.setVisibility(VISIBLE);
                    staggeredVerExtra.setVisibility(GONE);
                    break;
                case R.id.radio_staggered_versioning:
                    simpleVerExtra.setVisibility(GONE);
                    staggeredVerExtra.setVisibility(VISIBLE);
                    break;
                case R.id.radio_no_versioning:
                default:
                    simpleVerExtra.setVisibility(GONE);
                    staggeredVerExtra.setVisibility(GONE);
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

    static class DeviceCheckBox extends CheckBox {
        final DeviceConfig device;
        DeviceCheckBox(Context context, DeviceConfig device) {
            super(context);
            this.device = device;
        }
    }
}
