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
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;
import syncthing.android.R;
import syncthing.api.SessionManager;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.Ignores;

/**
 * Created by drew on 3/23/15.
 */
@ScreenScope
public class EditIgnoresPresenter extends EditPresenter<CoordinatorLayout> {

    Subscription initSubscription;
    Ignores ignores;

    @Inject
    public EditIgnoresPresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            ToolbarOwner toolbarOwner,
            EditPresenterConfig config
    ) {
        super(manager, dialogPresenter, activityResultContoller, toolbarOwner, config);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        unsubscribe(initSubscription);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!wasPreviouslyLoaded && savedInstanceState != null) {
            ignores = (Ignores) savedInstanceState.getSerializable("ignores");
        } else if (!wasPreviouslyLoaded) {
            getIgnores();
        }
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putSerializable("ignores", ignores);
    }

    void getIgnores() {
        initSubscription = controller.getIgnores(folderId,
                ignrs -> {
                    ignores = ignrs;
                    notifyChange(syncthing.android.BR.ignoresEnabled);
                    notifyChange(syncthing.android.BR.ignoresText);
                    notifyChange(syncthing.android.BR.ignoresPath);
                },
                t -> {
                    //TODO
                }
        );
    }

    @Bindable
    public boolean isIgnoresEnabled() {
        return ignores != null;
    }

    @Bindable
    public String getIgnoresPath() {
        FolderConfig f = controller.getFolder(folderId);
        if (f != null) {
            return f.path + ".stignore";
        } else {
            return ".stignore";
        }
    }

    @Bindable
    public String getIgnoresText() {
        if (ignores != null) {
            return StringUtils.join(ignores.ignore, "\n");
        } else {
            return null;
        }
    }

    public void setIgnores(CharSequence text, boolean notify) {
        if (isIgnoresEnabled() && validateIgnores(text)) {
            ignores.ignore = StringUtils.split(StringUtils.isEmpty(text) ? "" : text.toString(), "\n");
        }
    }

    public Action1<CharSequence> actionSetIgnores = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setIgnores(charSequence, false);
        }
    };

    boolean validateIgnores(CharSequence raw) {
        return true;//todo
    }

    public void saveIgnores(View btn) {
        if (!isIgnoresEnabled()) return;
        unsubscribe(saveSubscription);
        onSaveStart();
        saveSubscription = controller.editIgnores(folderId, ignores,
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
