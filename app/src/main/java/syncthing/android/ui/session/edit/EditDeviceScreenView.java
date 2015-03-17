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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import mortar.dagger2support.DaggerService;
import syncthing.android.R;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;

/**
 * Created by drew on 3/16/15.
 */
public class EditDeviceScreenView extends ScrollView {

    @InjectView(R.id.edit_device_id) EditText editDeviceId;
    @InjectView(R.id.btn_scanqr) ImageButton btnScanQr;
    @InjectView(R.id.desc_device_id) TextView descDeviceId;
    @InjectView(R.id.desc_device_id2) TextView descDeviceId2;
    @InjectView(R.id.error_device_id_blank) TextView errorDeviceIdBlank;
    @InjectView(R.id.error_device_id_invalid) TextView errorDeviceIdInvalid;
    @InjectView(R.id.edit_device_name) TextView editDeviceName;
    @InjectView(R.id.edit_addresses) TextView editAddresses;
    @InjectView(R.id.check_compression) CheckBox checkCompression;
    @InjectView(R.id.check_introducer) CheckBox checkIntroducer;
    @InjectView(R.id.share_folders_container) ViewGroup shareFoldersContainer;
    @InjectView(R.id.btn_delete) Button btnDelete;

    final EditDevicePresenter presenter;

    boolean isAdd;
    AlertDialog errorDialog;

    public EditDeviceScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            presenter = null;
        } else {
            presenter = DaggerService.<EditDeviceComponent>getDaggerComponent(getContext()).presenter();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        if(!isInEditMode()) {
            presenter.takeView(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
        dismissErrorDialog();
    }

    @OnClick(R.id.btn_scanqr)
    void scanQr() {
        presenter.startQRScannerActivity();
    }

    @OnClick(R.id.btn_delete)
    void onDelete() {
        presenter.deleteDevice();
    }

    @OnClick(R.id.btn_cancel)
    void onCancel(){
        presenter.dismissDialog();
    }

    @OnClick(R.id.btn_save)
    void onSave() {
        DeviceConfig device = new DeviceConfig();
        if (!presenter.validateDeviceId(editDeviceId.getText().toString(), false)) {
            return;
        }
        device.deviceID = editDeviceId.getText().toString();
        device.name = editDeviceName.getText().toString();
        if (!presenter.validateAddresses(editAddresses.getText().toString())) {
            return;
        }
        device.addresses = EditorUtils.rollArray(editAddresses.getText().toString());
        device.compression = checkCompression.isChecked();
        device.introducer = checkIntroducer.isChecked();

        Map<String, Boolean> folders = new HashMap<>();
        for (int ii=0; ii<shareFoldersContainer.getChildCount(); ii++) {
            View child = shareFoldersContainer.getChildAt(ii);
            if (child instanceof FolderCheckBox) {
                FolderCheckBox cb = (FolderCheckBox) child;
                folders.put(cb.folder.id, cb.isChecked());
            }
        }

        presenter.saveDevice(device, folders);
    }

    void initialize(boolean isAdd, DeviceConfig device, Collection<FolderConfig> folders) {
        this.isAdd = isAdd;
        if (!isAdd) {
            editDeviceId.setText(device.deviceID);
            editDeviceId.setEnabled(false);
            btnScanQr.setVisibility(GONE);
            descDeviceId.setVisibility(GONE);
            descDeviceId2.setVisibility(GONE);
            editDeviceName.setText(device.name);
            editAddresses.setText(EditorUtils.unrollArray(device.addresses));
            checkCompression.setChecked(device.compression);
            checkIntroducer.setChecked(device.introducer);
        } else {
            //set nice defaults
            DeviceConfig nd = new DeviceConfig();
            editAddresses.setText(EditorUtils.unrollArray(nd.addresses));
            checkCompression.setChecked(nd.compression);
            checkIntroducer.setChecked(nd.introducer);
        }

        shareFoldersContainer.removeAllViews();
        for (FolderConfig f : folders) {
            FolderCheckBox checkBox = new FolderCheckBox(getContext(), f);
            checkBox.setText(f.id);
            if (!isAdd) {
                for (FolderDeviceConfig d : f.devices) {
                    if (StringUtils.equals(d.deviceID, device.deviceID)) {
                        checkBox.setChecked(true);
                        break;
                    }
                }
            }
            shareFoldersContainer.addView(checkBox);
        }

        btnDelete.setVisibility(isAdd ? GONE : VISIBLE);

    }

    @OnTextChanged(R.id.edit_device_id)
    void onDeviceIdChange(CharSequence text) {
        if (isAdd && !StringUtils.isEmpty(text)) {
            if (presenter.validateDeviceId(text, true)) {
                descDeviceId.setVisibility(VISIBLE);
                descDeviceId2.setVisibility(VISIBLE);
                errorDeviceIdBlank.setVisibility(GONE);
                errorDeviceIdInvalid.setVisibility(GONE);
            }
        }
    }

    void notifyDeviceIdEmpty() {
        descDeviceId.setVisibility(GONE);
        descDeviceId2.setVisibility(GONE);
        errorDeviceIdBlank.setVisibility(VISIBLE);
        errorDeviceIdInvalid.setVisibility(GONE);
    }

    void notifyDeviceIdInvalid() {
        descDeviceId.setVisibility(GONE);
        descDeviceId2.setVisibility(GONE);
        errorDeviceIdBlank.setVisibility(GONE);
        errorDeviceIdInvalid.setVisibility(VISIBLE);
    }

    void notifyInvalidAddresses() {
        //TODO
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

    static class FolderCheckBox extends CheckBox {
        final FolderConfig folder;
        FolderCheckBox(Context context, FolderConfig folder) {
            super(context);
            this.folder = folder;
        }
    }

}
