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
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.Compression;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;

/**
 * Created by drew on 3/16/15.
 */
public class EditDeviceScreenView extends CoordinatorLayout {

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.edit_device_id) EditText editDeviceId;
    @InjectView(R.id.btn_scanqr) ImageButton btnScanQr;
    @InjectView(R.id.desc_device_id) TextView descDeviceId;
    @InjectView(R.id.desc_device_id2) TextView descDeviceId2;
    @InjectView(R.id.error_device_id_blank) TextView errorDeviceIdBlank;
    @InjectView(R.id.error_device_id_invalid) TextView errorDeviceIdInvalid;
    @InjectView(R.id.edit_device_name) TextView editDeviceName;
    @InjectView(R.id.edit_addresses) TextView editAddresses;
    @InjectView(R.id.radio_group_compression) RadioGroup groupCompression;
    @InjectView(R.id.radio_all_compression) RadioButton rdioAllCompression;
    @InjectView(R.id.radio_meta_compression) RadioButton rdioMetaCompression;
    @InjectView(R.id.radio_no_compression) RadioButton rdioNoCompression;
    @InjectView(R.id.check_introducer) CheckBox checkIntroducer;
    @InjectView(R.id.share_folders_container) ViewGroup shareFoldersContainer;
    @InjectView(R.id.btn_delete) Button btnDelete;

    @Inject ToolbarOwner mToolbarOwner;
    @Inject EditDevicePresenter mPresenter;

    boolean isAdd;
    DeviceConfig device;

    public EditDeviceScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            EditDeviceComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        groupCompression.setOnCheckedChangeListener(compressionChangedListener);
        mPresenter.takeView(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mToolbarOwner.attachToolbar(toolbar);
        mToolbarOwner.setConfig(mPresenter.getToolbarConfig());
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
    }

    @OnClick(R.id.btn_scanqr)
    void scanQr() {
        mPresenter.startQRScannerActivity();
    }

    @OnClick(R.id.btn_delete)
    void onDelete() {
        mPresenter.deleteDevice();
    }

    @OnClick(R.id.btn_cancel)
    void onCancel(){
        mPresenter.dismissDialog();
    }

    @OnClick(R.id.btn_save)
    void onSave() {
        if (!mPresenter.validateDeviceId(editDeviceId.getText().toString(), false)) {
            return;
        }
        device.deviceID = editDeviceId.getText().toString();
        device.name = editDeviceName.getText().toString();
        if (!mPresenter.validateAddresses(editAddresses.getText().toString())) {
            return;
        }
        device.addresses = SyncthingUtils.rollArray(editAddresses.getText().toString());
        switch (groupCompression.getCheckedRadioButtonId()) {
            case R.id.radio_all_compression:
                device.compression = Compression.ALWAYS;
                break;
            case R.id.radio_meta_compression:
                device.compression = Compression.METADATA;
                break;
            case R.id.radio_no_compression:
            default:
                device.compression = Compression.NEVER;
                break;
        }
        device.introducer = checkIntroducer.isChecked();

        Map<String, Boolean> folders = new HashMap<>();
        for (int ii=0; ii<shareFoldersContainer.getChildCount(); ii++) {
            View child = shareFoldersContainer.getChildAt(ii);
            if (child instanceof FolderCheckBox) {
                FolderCheckBox cb = (FolderCheckBox) child;
                folders.put(cb.folder.id, cb.isChecked());
            }
        }

        mPresenter.saveDevice(folders);
    }

    void initialize(boolean isAdd, DeviceConfig device, Collection<FolderConfig> folders, boolean fromsavedstate) {
        this.isAdd = isAdd;
        this.device = device;
        if (fromsavedstate) return;
        if (!isAdd) {
            editDeviceId.setText(device.deviceID);
            editDeviceId.setEnabled(false);
            btnScanQr.setVisibility(GONE);
            descDeviceId.setVisibility(GONE);
            descDeviceId2.setVisibility(GONE);
            editDeviceName.setText(device.name);
            editAddresses.setText(SyncthingUtils.unrollArray(device.addresses));
            setCompression(device.compression);
            checkIntroducer.setChecked(device.introducer);
        } else {
            //set nice defaults
            editAddresses.setText(SyncthingUtils.unrollArray(device.addresses));
            setCompression(device.compression);
            checkIntroducer.setChecked(device.introducer);
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

    void setCompression(Compression compression) {
        switch (compression) {
            case ALWAYS:
                rdioAllCompression.setChecked(true);
                break;
            case METADATA:
                rdioMetaCompression.setChecked(true);
                break;
            case NEVER:
            default:
                rdioNoCompression.setChecked(true);
                break;

        }
    }

    @OnTextChanged(R.id.edit_device_id)
    void onDeviceIdChange(CharSequence text) {
        if (isAdd && !StringUtils.isEmpty(text)) {
            if (mPresenter.validateDeviceId(text.toString(), true)) {
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

    final RadioGroup.OnCheckedChangeListener compressionChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_all_compression:
                    break;
                case R.id.radio_meta_compression:
                    break;
                case R.id.radio_no_compression:
                default:
                    break;
            }
        }
    };

    @SuppressLint("ViewConstructor")
    static class FolderCheckBox extends CheckBox {
        final FolderConfig folder;
        FolderCheckBox(Context context, FolderConfig folder) {
            super(context);
            this.folder = folder;
        }
    }

}
