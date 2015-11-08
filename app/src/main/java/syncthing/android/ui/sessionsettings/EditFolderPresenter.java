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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.View;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ActionBarMenuConfig;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.ActivityResultsListener;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortarfragment.FragmentManagerOwner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import mortar.MortarScope;
import rx.Subscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.ManageActivity;
import syncthing.android.ui.common.ActivityRequestCodes;
import syncthing.android.ui.folderpicker.FolderPickerFragment;
import syncthing.api.SessionManager;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import syncthing.api.model.PullOrder;
import syncthing.api.model.VersioningConfig;
import syncthing.api.model.VersioningType;
import timber.log.Timber;

import static syncthing.android.ui.sessionsettings.EditPresenterConfig.INVALID_ID;

/**
 * Created by drew on 3/16/15.
 */
@ScreenScope
public class EditFolderPresenter extends EditPresenter<EditFolderScreenView> implements ActivityResultsListener {

    final FragmentManagerOwner fm;

    FolderConfig origFolder;

    Subscription deleteSubscription;

    @Inject
    public EditFolderPresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            EditPresenterConfig config,
            FragmentManagerOwner fm
    ) {
        super(manager, dialogPresenter, activityResultContoller, config);
        this.fm = fm;
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
                origFolder = FolderConfig.withDefaults();
                if (!INVALID_ID.equals(folderId)) {
                    origFolder.id = folderId;
                }
                if (!INVALID_ID.equals(deviceId)) {
                    origFolder.devices = Collections.singletonList(new FolderDeviceConfig(deviceId));
                }
            } else {
                origFolder = SerializationUtils.clone(controller.getFolder(folderId));
            }
        } else {
            origFolder = (FolderConfig) savedInstanceState.getSerializable("folder");
        }
        if (origFolder == null) {
            Timber.e("Folder was null! cannot continue.");
            activityResultsController.setResultAndFinish(Activity.RESULT_CANCELED, new Intent());
            return;
        }
        getView().initialize(controller.getRemoteDevices(), controller.getSystemInfo(), savedInstanceState != null);
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putSerializable("folder", origFolder);
    }

    @Override
    public ActionBarConfig getToolbarConfig() {
        return super.getToolbarConfig().buildUpon()
                .setMenuConfig(ActionBarMenuConfig.builder()
                .withMenu(R.menu.folder_ignores)
                .setActionHandler((context, id) -> {
                    switch (id) {
                        case R.id.edit_ignores:
                            openIgnoresEditor(context);
                            return true;
                        default:
                            return false;
                    }
                }).build()).build();
    }

    boolean validateFolderId(CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            getView().notifyEmptyFolderId(false);
            return false;
        } else if (!text.toString().matches("^[-.\\w]{1,64}$")) {
            getView().notifyInvalidFolderId(false);
            return false;
        } else if (!isFolderIdUnique(text, controller.getFolders())) {
            getView().notifyNotUniqueFolderId(false);
            return false;
        } else {
            getView().notifyEmptyFolderId(true);
            getView().notifyInvalidFolderId(true);
            getView().notifyNotUniqueFolderId(true);
            return true;
        }
    }

    static boolean isFolderIdUnique(CharSequence text, Collection<FolderConfig> folders) {
        for (FolderConfig f : folders) if (StringUtils.equals(f.id, text)) return false;
        return true;
    }

    boolean validateFolderPath(CharSequence text) {
        boolean valid;
        if (StringUtils.isEmpty(text)) {
            valid = false;
        } else {
            valid = true;
        }
        getView().notifyEmptyFolderPath(valid);
        return valid;
    }

    boolean validateRescanInterval(CharSequence text) {
        boolean valid;
        if (!StringUtils.isNumeric(text) || Integer.decode(text.toString()) < 0) {
            valid = false;
        } else {
            valid = true;
        }
        getView().notifyInvalidRescanInterval(valid);
        return valid;
    }

    boolean validateTrashCanVersioningKeep(CharSequence text) {
        if (StringUtils.isEmpty(text) || !StringUtils.isNumeric(text)
                || Integer.parseInt(text.toString()) < 0) {
            getView().notifyTrashCanVersioningKeepInvalid(false);
            return false;
        } else {
            getView().notifyTrashCanVersioningKeepInvalid(true);
            return true;
        }
    }

    boolean validateSimpleVersioningKeep(CharSequence text) {
        if (StringUtils.isEmpty(text) || !StringUtils.isNumeric(text)) {
            getView().notifySimpleVersioningKeepEmpty(false);
            return false;
        } else if (!StringUtils.isNumeric(text) || Integer.decode(text.toString()) < 1) {
            getView().notifySimpleVersioningKeepInvalid(false);
            return false;
        } else {
            getView().notifySimpleVersioningKeepEmpty(true);
            getView().notifySimpleVersioningKeepInvalid(true);
            return true;
        }
    }

    boolean validateStaggeredMaxAge(CharSequence text) {
        boolean valid;
        if (StringUtils.isEmpty(text)
                || !StringUtils.isNumeric(text)
                || Integer.decode(text.toString()) < 0) {
            valid = false;
        } else {
            valid = true;
        }
        getView().notifyStaggeredMaxAgeInvalid(valid);
        return valid;
    }

    boolean validateExternalVersioningCmd(CharSequence text) {
        boolean valid;
        if (StringUtils.isEmpty(text)) {
            valid = false;
        } else {
            valid = true;
        }
        getView().notifyExternalVersioningCmdInvalid(valid);
        return valid;
    }

    public void saveFolder(View btn) {
        if(!hasView()) return;
        EditFolderScreenView v = getView();
        if (isAdd && !validateFolderId(v.binding.editFolderId.getText().toString())) {
            return;
        }
        origFolder.id = v.binding.editFolderId.getText().toString();
        if (isAdd && !validateFolderPath(v.binding.editFolderPath.getText().toString())) {
            return;
        }
        origFolder.path = v.binding.editFolderPath.getText().toString();
        if (!validateRescanInterval(v.binding.editRescanInterval.getText().toString())) {
            return;
        }
        origFolder.rescanIntervalS = Integer.decode(v.binding.editRescanInterval.getText().toString());
        origFolder.readOnly = v.binding.checkFolderMaster.isChecked();
        origFolder.ignorePerms = v.binding.checkIgnorePermissions.isChecked();

        switch (v.binding.radioGroupPullorder.getCheckedRadioButtonId()) {
            case R.id.radio_pullorder_alphabetic:
                origFolder.order = PullOrder.ALPHABETIC;
                break;
            case R.id.radio_pullorder_smallestfirst:
                origFolder.order = PullOrder.SMALLESTFIRST;
                break;
            case R.id.radio_pullorder_largestfirst:
                origFolder.order = PullOrder.LARGESTFIRST;
                break;
            case R.id.radio_pullorder_oldestfirst:
                origFolder.order = PullOrder.OLDESTFIRST;
                break;
            case R.id.radio_pullorder_newestfirst:
                origFolder.order = PullOrder.NEWESTFIRST;
                break;
            case R.id.radio_pullorder_random:
            default:
                origFolder.order = PullOrder.RANDOM;
                break;
        }

        switch (v.binding.radioGroupVersioning.getCheckedRadioButtonId()) {
            case R.id.radio_trashcan_versioning:
                origFolder.versioning = new VersioningConfig();
                origFolder.versioning.type = VersioningType.TRASHCAN;
                if (!validateTrashCanVersioningKeep(v.binding.editTrashcanVersioningKeep.getText().toString())) {
                    return;
                }
                origFolder.versioning.params.cleanoutDays = v.binding.editTrashcanVersioningKeep.getText().toString();
                break;
            case R.id.radio_simple_versioning:
                origFolder.versioning = new VersioningConfig();
                origFolder.versioning.type = VersioningType.SIMPLE;
                if (!validateSimpleVersioningKeep(v.binding.editSimpleVersioningKeep.getText().toString())) {
                    return;
                }
                origFolder.versioning.params.keep = v.binding.editSimpleVersioningKeep.getText().toString();
                break;
            case R.id.radio_staggered_versioning:
                origFolder.versioning = new VersioningConfig();
                origFolder.versioning.type = VersioningType.STAGGERED;
                if (!validateStaggeredMaxAge(v.binding.editStaggeredMaxAge.getText().toString())) {
                    return;
                }
                origFolder.versioning.params.maxAge = SyncthingUtils.daysToSeconds(v.binding.editStaggeredMaxAge.getText().toString());
                //todo notify empty
                if (!StringUtils.isEmpty(v.binding.editStaggeredPath.getText().toString())) {
                    origFolder.versioning.params.versionPath = v.binding.editStaggeredPath.getText().toString();
                }
                break;
            case R.id.radio_external_versioning:
                origFolder.versioning = new VersioningConfig();
                origFolder.versioning.type = VersioningType.EXTERNAL;
                if (!validateExternalVersioningCmd(v.binding.editExternalVersioningCommand.getText().toString())) {
                    return;
                }
                origFolder.versioning.params.command = v.binding.editExternalVersioningCommand.getText().toString();
                break;
            case R.id.radio_no_versioning:
            default:
                origFolder.versioning = new VersioningConfig();
                break;
        }

        List<FolderDeviceConfig> devices = new ArrayList<>();
        for (int ii=0; ii< v.binding.shareDevicesContainer.getChildCount(); ii++) {
            View c = v.binding.shareDevicesContainer.getChildAt(ii);
            if (c instanceof EditFolderScreenView.DeviceCheckBox) {
                EditFolderScreenView.DeviceCheckBox cb = (EditFolderScreenView.DeviceCheckBox) c;
                if (cb.isChecked()) {
                    devices.add(new FolderDeviceConfig(cb.device.deviceID));
                }
            }
        }
        origFolder.devices = devices;

        unsubscribe(saveSubscription);
        onSaveStart();
        saveSubscription = controller.editFolder(origFolder,
                this::onSavefailed,
                this::onSaveSuccessfull
        );
    }

    public void deleteFolder(View btn) {
        unsubscribe(deleteSubscription);
        onSaveStart();
        deleteSubscription = controller.deleteFolder(origFolder,
                this::onSavefailed,
                this::onSaveSuccessfull
        );
    }

    public void openIgnoresEditor(Context context) {
        EditIgnoresFragment f = EditIgnoresFragment.ni(context, credentials, folderId);
        fm.replaceMainContent(f, true);
    }

    public void openFolderPicker(View btn) {
        if (!hasView()) return;
        EditFolderScreenView v = getView();
        String home = Environment.getExternalStorageDirectory().getAbsolutePath();
        Editable editText = v.binding.editFolderPath.getText();
        String path = null;
        if (editText != null) {
            path = editText.toString();
            if (StringUtils.equals(path, home) || StringUtils.isEmpty(path)) {
                path = home;
            } else if (StringUtils.endsWith(path, "/")) {
                path = path.substring(0, path.length() - 1);
            } else if (path.lastIndexOf("/") > 0) {
                //we want the last directory they inputed not any partial name in there
                path = path.substring(0, path.lastIndexOf("/"));
            }
        }
        if (StringUtils.isEmpty(path)) {
            path = home;
        }
        Intent i = new Intent(v.getContext(), ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, FolderPickerFragment.NAME)
                .putExtra(ManageActivity.EXTRA_ARGS, FolderPickerFragment.makeArgs(credentials, path))
                .putExtra(ManageActivity.EXTRA_UP_IS_BACK, true);
        activityResultsController.startActivityForResult(i, ActivityRequestCodes.FOLDER_PICKER, null);
    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCodes.FOLDER_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                if (hasView()) {
                    String path = data.getStringExtra("path");
                    getView().binding.editFolderPath.setText(path);
                }//else TODO
            }
            return true;
        } else {
            return false;
        }
    }

}
