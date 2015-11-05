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

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.ActivityResultsListener;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortarfragment.FragmentManagerOwner;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import mortar.MortarScope;
import rx.Subscription;
import syncthing.android.ui.ManageActivity;
import syncthing.android.ui.common.ActivityRequestCodes;
import syncthing.android.ui.folderpicker.FolderPickerFragment;
import syncthing.api.SessionManager;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;

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
        getView().initialize(isAdd, origFolder, controller.getRemoteDevices(), controller.getSystemInfo(), savedInstanceState != null);
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putSerializable("folder", origFolder);
    }

    void openFolderPicker(Context context, String base) {
        Intent i = new Intent(context, ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, FolderPickerFragment.NAME)
                .putExtra(ManageActivity.EXTRA_ARGS, FolderPickerFragment.makeArgs(credentials, base))
                .putExtra(ManageActivity.EXTRA_UP_IS_BACK, true);
        activityResultsController.startActivityForResult(i, ActivityRequestCodes.FOLDER_PICKER, null);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCodes.FOLDER_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                if (hasView()) {
                    String path = data.getStringExtra("path");
                    getView().editFolderPath.setText(path);
                }//else TODO
            }
            return true;
        } else {
            return false;
        }
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

    void saveFolder() {
        unsubscribe(saveSubscription);
        onSaveStart();
        saveSubscription = controller.editFolder(origFolder,
                this::onSavefailed,
                this::onSaveSuccessfull
        );
    }

    void deleteFolder() {
        unsubscribe(deleteSubscription);
        onSaveStart();
        deleteSubscription = controller.deleteFolder(origFolder,
                this::onSavefailed,
                this::onSaveSuccessfull
        );
    }

    void openIgnoresEditor(Context context) {
        EditIgnoresFragment f = EditIgnoresFragment.ni(context, credentials, folderId);
        fm.replaceMainContent(f, true);
    }

}
