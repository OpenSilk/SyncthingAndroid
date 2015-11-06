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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;

import javax.inject.Inject;

import rx.Subscription;
import syncthing.android.R;
import syncthing.api.SessionManager;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.Ignores;
import syncthing.api.model.SystemInfo;

/**
 * Created by drew on 3/23/15.
 */
@ScreenScope
public class EditIgnoresPresenter extends EditPresenter<EditIgnoresScreenView> {

    Subscription initSubscription;
    boolean isInitialized;
    Ignores ignores;

    @Inject
    public EditIgnoresPresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            EditPresenterConfig config
    ) {
        super(manager, dialogPresenter, activityResultContoller, config);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        unsubscribe(initSubscription);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (savedInstanceState != null) {
            isInitialized = savedInstanceState.getBoolean("initd");
            ignores = (Ignores) savedInstanceState.getSerializable("ignores");
        }
        if (!isInitialized && ignores != null) {
            FolderConfig f = controller.getFolder(folderId);
            SystemInfo s = controller.getSystemInfo();
            getView().initialize(f, s, ignores);
        } else {
            getIgnores();
        }
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putBoolean("initd", isInitialized);
        outState.putSerializable("ignores", ignores);
    }

    void getIgnores() {
        initSubscription = controller.getIgnores(folderId,
                ignrs -> {
                    if (hasView()) {
                        FolderConfig f = controller.getFolder(folderId);
                        SystemInfo s = controller.getSystemInfo();
                        getView().initialize(f, s, ignrs);
                        isInitialized = true;
                    } else {
                        isInitialized = false;
                        ignores = ignrs;
                    }
                },
                t -> {
                    //TODO
                    if (hasView()) {

                    }
                }
        );
    }

    boolean validateIgnores(CharSequence raw) {
        return true;//todo
    }

    public void saveIgnores(View btn) {
        if (!hasView()) return;
        EditIgnoresScreenView v = getView();
        if (!validateIgnores(v.binding.editIgnores.getText().toString())) {
            return;
        }
        CharSequence raw = v.binding.editIgnores.getText().toString();
        Ignores i = new Ignores();
        i.ignore = StringUtils.split(raw.toString(), "\n");
        unsubscribe(saveSubscription);
        onSaveStart();
        saveSubscription = controller.editIgnores(folderId, i,
                ignrs -> {},
                this::onSavefailed,
                this::onSaveSuccessfull
        );
    }

    public void openHelp(View btn) {
        if (hasView()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getView().getContext().getString(R.string.ignore_files_help)));
                activityResultsController.startActivityForResult(intent, 0, null);
            } catch (ActivityNotFoundException e) {
                //Better never happens
            }
        }
    }

}
