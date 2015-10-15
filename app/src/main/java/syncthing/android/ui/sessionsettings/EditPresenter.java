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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogFactory;
import org.opensilk.common.ui.mortar.DialogPresenter;

import mortar.ViewPresenter;
import rx.Subscription;
import syncthing.android.R;
import syncthing.api.Session;
import syncthing.api.SessionController;
import syncthing.api.SessionManager;

/**
 * Created by drew on 3/23/15.
 */
public class EditPresenter<V extends View> extends ViewPresenter<V> {

    protected final SessionManager manager;
    protected final SessionController controller;
    protected final String folderId;
    protected final String deviceId;
    protected final boolean isAdd;
    protected final Session session;
    protected final DialogPresenter dialogPresenter;
    protected final ActivityResultsController activityResultsController;

    protected Subscription saveSubscription;

    public EditPresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            EditPresenterConfig config
    ) {
        this.manager = manager;
        this.dialogPresenter = dialogPresenter;
        this.activityResultsController = activityResultContoller;
        this.session = manager.acquire(config.credentials);
        this.controller = this.session.controller();
        this.folderId = config.folderId;
        this.deviceId = config.deviceId;
        this.isAdd = config.isAdd;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        if (saveSubscription != null) {
            saveSubscription.unsubscribe();
        }
        manager.release(session);
    }

    //TODO save saving state and restore

    protected void onSaveStart() {
        dialogPresenter.showDialog(
                new DialogFactory() {
                    @Override
                    public Dialog call(Context context) {
                        ProgressDialog mProgressDialog = new ProgressDialog(getView().getContext());
                        mProgressDialog.setMessage(getView().getResources().getString(R.string.saving_config_dots));
                        return mProgressDialog;
                    }
                }
        );
    }

    protected void onSaveSuccessfull() {
        dialogPresenter.dismissDialog();
        if (hasView()) {
            Toast.makeText(getView().getContext(), R.string.config_saved, Toast.LENGTH_SHORT).show();
        }
        dismissDialog();
    }

    protected void onSavefailed(Throwable e) {
        final String msg = e.getMessage();
        dialogPresenter.showDialog(new DialogFactory() {
            @Override
            public Dialog call(Context context) {
                AlertDialog mErrorDialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(msg)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
                return mErrorDialog;
            }
        });
    }

    protected void dismissDialog() {
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, null);
    }
}
