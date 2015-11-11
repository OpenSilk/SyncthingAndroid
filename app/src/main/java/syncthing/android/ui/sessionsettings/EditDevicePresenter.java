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
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.widget.RxCompoundButton;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.ActivityResultsListener;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import mortar.MortarScope;
import rx.Subscription;
import rx.functions.Action1;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ActivityRequestCodes;
import syncthing.api.SessionManager;
import syncthing.api.model.Compression;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import timber.log.Timber;

/**
 * Created by drew on 3/16/15.
 */
@ScreenScope
public class EditDevicePresenter extends EditPresenter<CoordinatorLayout> implements ActivityResultsListener {

    DeviceConfig originalDevice;
    TreeMap<String, Boolean> sharedFolders;

    Subscription deleteSubscription;

    String errorDeviceId;
    String errorAddresses;

    @Inject
    public EditDevicePresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            ToolbarOwner toolbarOwner,
            EditPresenterConfig config
    ) {
        super(manager, dialogPresenter, activityResultContoller, toolbarOwner, config);
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

    @Override @SuppressWarnings("unchecked")
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!wasPreviouslyLoaded && savedInstanceState != null) {
            originalDevice = (DeviceConfig) savedInstanceState.getSerializable("device");
            sharedFolders = (TreeMap<String, Boolean>) savedInstanceState.getSerializable("folders");
        } else if (!wasPreviouslyLoaded) {
            if (isAdd) {
                originalDevice = DeviceConfig.withDefaults();
            } else {
                DeviceConfig d = controller.getDevice(deviceId);
                if (d != null) {
                    originalDevice = d.clone();
                }
            }
            sharedFolders = new TreeMap<>();
            Collection<FolderConfig> folders = controller.getFolders();
            for (FolderConfig f : folders) {
                sharedFolders.put(f.id, false);
                if (!StringUtils.isEmpty(getDeviceID())) {
                    for (FolderDeviceConfig d : f.devices) {
                        if (StringUtils.equals(d.deviceID, getDeviceID())) {
                            sharedFolders.put(f.id, true);
                            break;
                        }
                    }
                }
            }
        }
        wasPreviouslyLoaded = true;
        if (originalDevice == null) {
            Timber.e("Null device! Cannot continue");
            dismissDialog();
        }
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putSerializable("device", originalDevice);
        outState.putSerializable("folders", sharedFolders);
    }

    @Bindable
    public boolean isAdd() {
        return isAdd;
    }

    @Bindable
    public String getDeviceID() {
        return originalDevice.deviceID;
    }

    public void setDeviceID(CharSequence deviceID) {
        if (validateDeviceId(deviceID, true)) {
            originalDevice.deviceID = deviceID.toString();
        }
    }

    public final Action1<CharSequence> actionSetDeviceID = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setDeviceID(charSequence);
        }
    };

    @Bindable
    public String getDeviceIDError() {
        return errorDeviceId;
    }

    public void setDeviceIDError(String error) {
        if (!StringUtils.equals(errorDeviceId, error)) {
            errorDeviceId = error;
            notifyChange(syncthing.android.BR.deviceIDError);
        }
    }

    boolean validateDeviceId(CharSequence text, boolean strict) {
        int e = 0;
        if (StringUtils.isEmpty(text)) {
            e = R.string.the_device_id_cannot_be_blank;
        } else if (strict && !StringUtils.remove(text.toString(), ' ').matches(("^[- \\w\\s]{50,64}$"))) {
            e = R.string.the_entered_device_id_does_not_look_valid_it_should_be_a_52_or_56_character_string_consisting_of_letters_and_numbers_with_spaces_and_dashes_being_optional;
        }
        if (hasView()) {
            setDeviceIDError(e != 0 ? getView().getContext().getString(e) : null);
        }
        return e == 0;
    }

    @Bindable
    public String getDeviceName() {
        return originalDevice.name;
    }

    public void setDeviceName(CharSequence name) {
        originalDevice.name = StringUtils.isEmpty(name) ? "" : name.toString();
    }

    public final Action1<CharSequence> actionSetDeviceName = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setDeviceID(charSequence);
        }
    };

    @Bindable
    public String getAddressesText() {
        return SyncthingUtils.unrollArray(originalDevice.addresses);
    }

    public void setAddresses(CharSequence addressesText) {
        if (validateAddresses(addressesText)) {
            originalDevice.addresses = SyncthingUtils.rollArray(addressesText.toString());
        }
    }

    public final Action1<CharSequence> actionSetAddresses = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setDeviceID(charSequence);
        }
    };

    @Bindable
    public String getAddressesError() {
        return errorAddresses;
    }

    public void setAddressesError(String text) {
        if (!StringUtils.equals(errorAddresses, text)) {
            errorAddresses = text;
            notifyChange(syncthing.android.BR.addressesError);
        }
    }

    boolean validateAddresses(CharSequence text) {
        boolean invalid = false;
        if (StringUtils.isEmpty(text.toString())) {
            invalid = true;
        } else {
            String s = text.toString().trim();
            if (!StringUtils.contains(s, ',')) {
                if (!StringUtils.equals(s, "dynamic") && !validateAddress(s)) {
                    invalid = true;
                }
            } else {
                String[] addrs = SyncthingUtils.rollArray(s);
                for (String addr : addrs) {
                    if (!validateAddress(addr.trim())) {
                        invalid = true;
                        break;
                    }
                }
            }
        }
        if (hasView()) {
            setAddressesError(invalid ? getView().getContext().getString(R.string.input_error) : null);
        }
        return !invalid;
    }

    boolean validateAddress(String addr) {
        return StringUtils.startsWith(addr.trim(), "tcp://") &&
                (SyncthingUtils.isIpAddressWithPort(StringUtils.removeStart(addr.trim(), "tcp://")) ||
                        SyncthingUtils.isDomainNameWithPort(StringUtils.removeStart(addr.trim(), "tcp://")));
    }

    @Bindable
    public Compression getCompression() {
        return originalDevice.compression;
    }

    public void setCompression(Compression compression) {
        originalDevice.compression = compression;
    }

    public final Action1<Integer> actionOnCompressionChanged = new Action1<Integer>() {
        @Override
        public void call(Integer checkedId) {
            switch (checkedId) {
                case R.id.radio_all_compression:
                    setCompression(Compression.ALWAYS);
                    break;
                case R.id.radio_meta_compression:
                    setCompression(Compression.METADATA);
                    break;
                case R.id.radio_no_compression:
                    setCompression(Compression.NEVER);
                    break;
            }
        }
    };

    @Bindable
    public boolean isIntroducer() {
        return originalDevice.introducer;
    }

    public void setIntroducer(boolean introducer) {
        originalDevice.introducer = introducer;
    }

    public final Action1<Boolean> actionSetIntroducer = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            setIntroducer(aBoolean);
        }
    };

    public void setFolderShared(String folderId, boolean shared) {
        sharedFolders.put(folderId, shared);
    }

    @BindingAdapter("addShareFolders")
    public static void addSharedFolders(LinearLayout shareFoldersContainer, EditDevicePresenter presenter) {
        if (presenter == null) return;
        shareFoldersContainer.removeAllViews();
        for (Map.Entry<String, Boolean> e : presenter.sharedFolders.entrySet()) {
            final String id = e.getKey();
            CheckBox checkBox = new CheckBox(shareFoldersContainer.getContext());
            checkBox.setText(id);
            checkBox.setChecked(e.getValue());
            shareFoldersContainer.addView(checkBox);
            presenter.bindingSubscriptions().add(RxCompoundButton.checkedChanges(checkBox)
                    .subscribe(checked -> {
                        presenter.setFolderShared(id, checked);
                    }));
        }
    }

    public void saveDevice(View btn) {
        boolean invalid = false;
        invalid |= errorDeviceId != null;
        invalid |= errorAddresses != null;
        if (invalid) {
            dialogPresenter.showDialog(context -> new AlertDialog.Builder(context)
                    .setTitle(R.string.input_error)
                    .setMessage(R.string.input_error_message)
                    .setPositiveButton(android.R.string.cancel, null)
                    .setNegativeButton(R.string.save, (d,w) -> {
                        saveDevice();
                    })
                    .create());
        } else {
            saveDevice();
        }
    }

    private void saveDevice() {
        unsubscribe(saveSubscription);
        onSaveStart();
        saveSubscription = controller.editDevice(originalDevice, sharedFolders,
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
                dialogPresenter.showDialog(context -> new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(R.string.no_qr_scanner_installed)
                        .setPositiveButton(android.R.string.ok, null)
                        .create());
            }
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCodes.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK && hasView()) {
                String id = data.getStringExtra("SCAN_RESULT");
                if (!StringUtils.isEmpty(id)) {
                    setDeviceID(id);
                    notifyChange(syncthing.android.BR.deviceID);
                }
            }
            return true;
        }
        return false;
    }
}
