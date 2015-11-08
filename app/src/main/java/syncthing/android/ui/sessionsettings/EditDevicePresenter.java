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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.ActivityResultsListener;
import org.opensilk.common.ui.mortar.DialogPresenter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import mortar.MortarScope;
import rx.Subscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ActivityRequestCodes;
import syncthing.api.SessionManager;
import syncthing.api.model.Compression;
import syncthing.api.model.DeviceConfig;
import timber.log.Timber;

/**
 * Created by drew on 3/16/15.
 */
@ScreenScope
public class EditDevicePresenter extends EditPresenter<EditDeviceScreenView> implements ActivityResultsListener {

    DeviceConfig originalDevice;

    Subscription deleteSubscription;

    @Inject
    public EditDevicePresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            EditPresenterConfig config
    ) {
        super(manager, dialogPresenter, activityResultContoller, config);
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        activityResultsController.register(scope, this);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        unsubscribe(deleteSubscription);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (savedInstanceState == null) {
            if (isAdd) {
                originalDevice = DeviceConfig.withDefaults();
            } else {
                DeviceConfig d = controller.getDevice(deviceId);
                if (d != null) {
                    originalDevice = d.clone();
                }
            }
        } else {
            originalDevice = (DeviceConfig) savedInstanceState.getSerializable("device");
        }
        if (originalDevice == null) {
            Timber.e("Null device! Cannot continue");
            dismissDialog();
        }
        getView().initialize(controller.getFolders(), savedInstanceState != null);
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putSerializable("device", originalDevice);
    }

    boolean validateDeviceId(CharSequence text, boolean strict) {
        if (StringUtils.isEmpty(text)) {
            getView().notifyDeviceIdEmpty();
            return false;
        } else if (strict && !text.toString().matches("^[- \\w\\s]{50,64}$")) {
            getView().notifyDeviceIdInvalid();
            return false;
        }
        return true;
    }

    boolean validateAddresses(CharSequence text) {
        return true;//TODO
    }

    public void saveDevice(View btn) {
        if (!hasView()) return;
        EditDeviceScreenView v = getView();
        if (!validateDeviceId(v.binding.editDeviceId.getText().toString(), false)) {
            return;
        }
        originalDevice.deviceID = v.binding.editDeviceId.getText().toString();
        originalDevice.name = v.binding.editDeviceName.getText().toString();
        if (!validateAddresses(v.binding.editAddresses.getText().toString())) {
            return;
        }
        originalDevice.addresses = SyncthingUtils.rollArray(v.binding.editAddresses.getText().toString());
        switch (v.binding.radioGroupCompression.getCheckedRadioButtonId()) {
            case R.id.radio_all_compression:
                originalDevice.compression = Compression.ALWAYS;
                break;
            case R.id.radio_meta_compression:
                originalDevice.compression = Compression.METADATA;
                break;
            case R.id.radio_no_compression:
            default:
                originalDevice.compression = Compression.NEVER;
                break;
        }
        originalDevice.introducer = v.binding.checkIntroducer.isChecked();

        Map<String, Boolean> folders = new HashMap<>();
        for (int ii=0; ii<v.binding.shareFoldersContainer.getChildCount(); ii++) {
            View child = v.binding.shareFoldersContainer.getChildAt(ii);
            if (child instanceof EditDeviceScreenView.FolderCheckBox) {
                EditDeviceScreenView.FolderCheckBox cb = (EditDeviceScreenView.FolderCheckBox) child;
                folders.put(cb.folder.id, cb.isChecked());
            }
        }
        unsubscribe(saveSubscription);
        onSaveStart();
        saveSubscription = controller.editDevice(originalDevice, folders,
                this::onSavefailed,
                this::onSaveSuccessfull
        );
    }

    public void deleteDevice(View btn) {
        unsubscribe(deleteSubscription);
        onSaveStart();
        deleteSubscription = controller.deleteDevice(originalDevice,
                this::onSavefailed,
                this::onSaveSuccessfull
        );
    }

    public void startQRScannerActivity(View btn) {
        Intent intentScan = new Intent("com.google.zxing.client.android.SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        try {
            activityResultsController.startActivityForResult(intentScan, ActivityRequestCodes.SCAN_QR, null);
        } catch (ActivityNotFoundException e) {
            if (hasView()) {
                //TODO
//                sessionPresenter.showError("",getView().getResources().getString(R.string.no_qr_scanner_installed));
            }
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCodes.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK && hasView()) {
                String id = data.getStringExtra("SCAN_RESULT");
                if (hasView()) {
                    getView().binding.editDeviceId.setText(id);
                }
            }
            return true;
        }
        return false;
    }
}
