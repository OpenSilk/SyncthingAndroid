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
import android.databinding.Bindable;
import android.os.Bundle;
import android.view.View;

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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import mortar.MortarScope;
import rx.Subscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.ManageActivity;
import syncthing.android.ui.common.ActivityRequestCodes;
import syncthing.android.ui.folderpicker.FolderPickerFragment;
import syncthing.api.SessionManager;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import syncthing.api.model.PullOrder;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;
import syncthing.api.model.VersioningExternal;
import syncthing.api.model.VersioningNone;
import syncthing.api.model.VersioningSimple;
import syncthing.api.model.VersioningStaggered;
import syncthing.api.model.VersioningTrashCan;
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
    VersioningTrashCan.Params trashCanParams = new VersioningTrashCan.Params();
    VersioningSimple.Params simpleParams = new VersioningSimple.Params();
    VersioningStaggered.Params staggeredParams = new VersioningStaggered.Params();
    VersioningExternal.Params externalParams = new VersioningExternal.Params();
    boolean newShare;
    TreeMap<String, Boolean> sharedDevices;

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

    @Override @SuppressWarnings("unchecked")
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!wasPreviouslyLoaded && savedInstanceState != null) {
            origFolder = (FolderConfig) savedInstanceState.getSerializable("folder");
            newShare = savedInstanceState.getBoolean("newShare");
            sharedDevices = (TreeMap<String, Boolean>) savedInstanceState.getSerializable("sharedDevices");
            initParams(origFolder);
        } else if (!wasPreviouslyLoaded) {
            if (isAdd) {
                origFolder = FolderConfig.withDefaults();
                if (!INVALID_ID.equals(folderId)) {
                    origFolder.id = folderId;
                    newShare = true;
                }
                SystemInfo sys = controller.getSystemInfo();
                if (sys != null) {
                    origFolder.path = controller.getSystemInfo().tilde;
                }
                Version ver = controller.getVersion();
                if (ver != null && StringUtils.equals(ver.os, "android")) {
                    //set ignore perms on android by default
                    origFolder.ignorePerms = true;
                }
            } else {
                FolderConfig f = controller.getFolder(folderId);
                if (f != null) {
                    origFolder = f.clone();
                }
            }
            initParams(origFolder);
            sharedDevices = new TreeMap<>();
            List<DeviceConfig> devices = controller.getRemoteDevices();
            for (DeviceConfig d : devices) {
                sharedDevices.put(d.deviceID, false);
                if (origFolder != null && origFolder.devices != null) {
                    for (FolderDeviceConfig d2 : origFolder.devices) {
                        if (StringUtils.equals(d2.deviceID, d.deviceID)) {
                            sharedDevices.put(d.deviceID, true);
                            break;
                        }
                    }
                }
            }
            if (!INVALID_ID.equals(deviceId) && sharedDevices.containsKey(deviceId)) {
                sharedDevices.put(deviceId, true);
            }
        }
        wasPreviouslyLoaded = true;
        if (origFolder == null) {
            Timber.e("Folder was null! cannot continue.");
            dismissDialog();
        }
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putSerializable("folder", origFolder);
        outState.putBoolean("newShare", newShare);
        outState.putSerializable("sharedDevices", sharedDevices);
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
        int e = 0;
        if (StringUtils.isEmpty(text.toString())) {
            e = R.string.the_folder_id_cannot_be_blank;
        } else if (!isFolderIdUnique(text.toString(), controller.getFolders())) {
            e = R.string.the_folder_id_must_be_unique;
        }
        if (hasView()) {
            CharSequence err = e != 0 ? getView().getContext().getString(e) : null;
            if (!StringUtils.equals(getView().binding.inputFolderId.getError(), err)) {
                getView().binding.inputFolderId.setError(err);
            }
        }
        return e == 0;
    }

    static boolean isFolderIdUnique(CharSequence text, Collection<FolderConfig> folders) {
        for (FolderConfig f : folders) if (StringUtils.equals(f.id, text)) return false;
        return true;
    }

    boolean validateFolderPath(CharSequence text) {
        boolean invalid = false;
        if (StringUtils.isEmpty(text)) {
            invalid = true;
        }
        if (hasView()) {
            CharSequence err = invalid ? getView().getContext().getString(R.string.the_folder_path_cannot_be_blank) : null;
            if (!StringUtils.equals(getView().binding.inputFolderPath.getError(), err)) {
                getView().binding.inputFolderPath.setError(err);
            }
        }
        return !invalid;
    }

    boolean validateRescanInterval(CharSequence text) {
        boolean invalid = false;
        //input disallows negative numbers and non numerals;
        if (StringUtils.isEmpty(text)) {
            invalid = true;
        }
        if (hasView()) {
            CharSequence err = invalid ? getView().getContext().getString(R.string.the_rescan_interval_must_be_a_nonnegative_number_of_seconds) : null;
            if (!StringUtils.equals(getView().binding.inputRescanInterval.getError(), err)) {
                getView().binding.inputRescanInterval.setError(err);
            }
        }
        return !invalid;
    }

    boolean validateTrashCanVersioningCleanDays(CharSequence text) {
        boolean invalid = false;
        //input disallows negative numbers and non numerals;
        if (StringUtils.isEmpty(text)) {
            invalid = true;
        }
        if (hasView()) {
            CharSequence err = invalid ? getView().getContext().getString(R.string.the_number_of_days_must_be_a_number_and_cannot_be_blank) : null;
            if (!StringUtils.equals(getView().binding.inputTrashcanVersioningKeep.getError(), err)) {
                getView().binding.inputTrashcanVersioningKeep.setError(err);
            }
        }
        return !invalid;
    }

    boolean validateSimpleVersioningKeep(CharSequence text) {
        int e = 0;
        //input disallows negative numbers and non numerals;
        if (StringUtils.isEmpty(text)) {
            e = R.string.the_number_of_versions_must_be_a_number_and_cannot_be_blank;
        } else if (Integer.parseInt(text.toString()) == 0) {
            e = R.string.you_must_keep_at_least_one_version;
        }
        if (hasView()) {
            CharSequence err = e != 0 ? getView().getContext().getString(e) : null;
            if (!StringUtils.equals(getView().binding.inputSimpleVersioningKeep.getError(), err)) {
                getView().binding.inputSimpleVersioningKeep.setError(err);
            }
        }
        return e == 0;
    }

    boolean validateStaggeredMaxAge(CharSequence text) {
        boolean invalid = false;
        //input disallows negative numbers and non numerals;
        if (StringUtils.isEmpty(text)) {
            invalid = true;
        }
        if (hasView()) {
            CharSequence err = invalid ? getView().getContext().getString(R.string.the_maximum_age_must_be_a_number_and_cannot_be_blank) : null;
            if (!StringUtils.equals(getView().binding.inputStaggeredMaxAge.getError(), err)) {
                getView().binding.inputStaggeredMaxAge.setError(err);
            }
        }
        return !invalid;
    }

    boolean validateExternalVersioningCmd(CharSequence text) {
        boolean invalid = false;
        if (StringUtils.isEmpty(text)) {
            invalid = true;
        }
        if (hasView()) {
            CharSequence err = invalid ? getView().getContext().getString(R.string.the_path_cannot_be_blank) : null;
            if (!StringUtils.equals(getView().binding.inputExternalVersioningCmd.getError(), err)) {
                getView().binding.inputExternalVersioningCmd.setError(err);
            }
        }
        return !invalid;
    }

    @Bindable
    public boolean isAdd() {
        return isAdd;
    }

    @Bindable
    public boolean isNewShare() {
        return newShare;
    }

    @Bindable
    public String getFolderID() {
        return origFolder.id;
    }

    public void setFolderId(CharSequence text) {
        if (!isAdd || newShare) {
            return;
        }
        if (validateFolderId(text)) {
            origFolder.id = text.toString();
        }
    }

    @Bindable
    public String getFolderPath() {
        return origFolder.path;
    }

    public void setFolderPath(CharSequence text) {
        if (!isAdd || newShare) {
            return;
        }
        if (validateFolderPath(text)) {
            origFolder.path = text.toString();
        }
    }

    @Bindable
    public String getRescanInterval() {
        return String.valueOf(origFolder.rescanIntervalS);
    }

    public void setRescanInterval(CharSequence text) {
        if (validateRescanInterval(text)) {
            origFolder.rescanIntervalS = Integer.valueOf(text.toString());
        }
    }

    @Bindable
    public boolean isReadOnly() {
        return origFolder.readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        origFolder.readOnly = readOnly;
    }

    @Bindable
    public boolean isIgnorePerms() {
        return origFolder.ignorePerms;
    }

    public void setIgnorePerms(boolean ignorePerms) {
        origFolder.ignorePerms = ignorePerms;
    }

    @Bindable
    public PullOrder getPullOrder() {
        return origFolder.order;
    }

    public void setPullOrder(PullOrder order) {
        origFolder.order = order;
    }

    @Bindable
    public VersioningType getVersioningType() {
        return origFolder.versioning.type;
    }

    public void setVersioningType(VersioningType type) {
        switch (type) {
            case TRASHCAN:
                origFolder.versioning = new VersioningTrashCan(type, trashCanParams);
                break;
            case SIMPLE:
                origFolder.versioning = new VersioningSimple(type, simpleParams);
                break;
            case STAGGERED:
                origFolder.versioning = new VersioningStaggered(type, staggeredParams);
                break;
            case EXTERNAL:
                origFolder.versioning = new VersioningExternal(type, externalParams);
                break;
            case NONE:
                origFolder.versioning = new VersioningNone(type);
                break;
        }
        notifyChange(syncthing.android.BR.versioningType);
    }

    private void initParams(FolderConfig f) {
        if (f == null || f.versioning == null) return;
        switch (origFolder.versioning.type) {
            case TRASHCAN:
                trashCanParams = (VersioningTrashCan.Params) origFolder.versioning.params;
                break;
            case SIMPLE:
                simpleParams = (VersioningSimple.Params) origFolder.versioning.params;
                break;
            case STAGGERED:
                staggeredParams = (VersioningStaggered.Params) origFolder.versioning.params;
                break;
            case EXTERNAL:
                externalParams = (VersioningExternal.Params) origFolder.versioning.params;
                break;
        }
    }

    @Bindable
    public VersioningTrashCan.Params getTrashCanParams() {
        return trashCanParams;
    }

    public void setTrashCanParamCleanDays(CharSequence text) {
        if (validateTrashCanVersioningCleanDays(text)) {
            trashCanParams.cleanoutDays = text.toString();
        }
    }

    @Bindable
    public VersioningSimple.Params getSimpleParams() {
        return simpleParams;
    }

    public void setSimpleParamKeep(CharSequence text) {
        if (validateSimpleVersioningKeep(text)) {
            simpleParams.keep = text.toString();
        }
    }

    @Bindable
    public VersioningStaggered.Params getStaggeredParams() {
        return staggeredParams;
    }

    @Bindable
    public String getStaggeredParamsMaxAge() {
        return SyncthingUtils.secondsToDays(staggeredParams.maxAge);
    }

    public void setStaggeredParamMaxAge(CharSequence text) {
        if (validateStaggeredMaxAge(text)) {
            staggeredParams.maxAge = SyncthingUtils.daysToSeconds(text.toString());
        }
    }

    public void setStaggeredParamPath(CharSequence text) {
        staggeredParams.versionPath = StringUtils.isEmpty(text) ? "" : text.toString();
    }

    @Bindable
    public VersioningExternal.Params getExternalParams() {
        return externalParams;
    }

    public void setExternalParamCmd(CharSequence text) {
        if (validateExternalVersioningCmd(text)) {
            externalParams.command = text.toString();
        }
    }

    public void setDeviceShared(String id, boolean shared) {
        sharedDevices.put(id, shared);
    }

    public void saveFolder(View btn) {
        if(!hasView()) return;
        //TODO check if any input fields are invalid
        List<FolderDeviceConfig> devices = new ArrayList<>();
        for (Map.Entry<String, Boolean> e : sharedDevices.entrySet()) {
            if (e.getValue()) {
                devices.add(new FolderDeviceConfig(e.getKey()));
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
        String path = getFolderPath();
        if (StringUtils.endsWith(path, "/")) {
            path = path.substring(0, path.length() - 1);
        } else if (path.lastIndexOf("/") > 0) {
            //we want the last directory they inputed not any partial name in there
            path = path.substring(0, path.lastIndexOf("/"));
        }
        Intent i = new Intent(btn.getContext(), ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, FolderPickerFragment.NAME)
                .putExtra(ManageActivity.EXTRA_ARGS, FolderPickerFragment.makeArgs(credentials, path))
                .putExtra(ManageActivity.EXTRA_UP_IS_BACK, true);
        activityResultsController.startActivityForResult(i, ActivityRequestCodes.FOLDER_PICKER, null);
    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCodes.FOLDER_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                String path = data.getStringExtra("path");
                if (path != null) {
                    setFolderPath(path);
                    notifyChange(syncthing.android.BR.folderPath);
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
