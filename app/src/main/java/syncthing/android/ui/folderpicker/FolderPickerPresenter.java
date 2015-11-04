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

package syncthing.android.ui.folderpicker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActionBarMenuConfig;
import org.opensilk.common.ui.mortar.ActionBarMenuHandler;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortarfragment.FragmentManagerOwner;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import mortar.ViewPresenter;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.api.Session;
import syncthing.api.SessionManager;
import timber.log.Timber;

/**
 * Created by drew on 11/3/15.
 */
@ScreenScope
public class FolderPickerPresenter extends ViewPresenter<FolderPickerView> {

    final Credentials credentials;
    final String path;
    final SessionManager sessionManager;
    final Session session;
    final FragmentManagerOwner fm;
    final ActivityResultsController activityResultsController;
    final DialogPresenter dialogPresenter;

    Subscription loaderSub;

    @Inject
    public FolderPickerPresenter(
            Credentials credentials,
            @Named("path") String path,
            SessionManager sessionManager,
            FragmentManagerOwner fm,
            ActivityResultsController activityResultsController,
            DialogPresenter dialogPresenter
    ) {
        this.credentials = credentials;
        this.path = StringUtils.endsWith(path, "/") ? path : path + "/";
        this.sessionManager = sessionManager;
        this.session = sessionManager.acquire(credentials);
        this.fm = fm;
        this.activityResultsController = activityResultsController;
        this.dialogPresenter = dialogPresenter;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        unsubscribeLoader();
        sessionManager.release(session);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        getView().showLoading();
        if (loaderSub == null || loaderSub.isUnsubscribed()) {
            loaderSub = session.controller().getAutoCompleteDirectoryList(path)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<List<String>>() {
                        @Override
                        public void onCompleted() {
                            if (hasView()) {
                                getView().onComplete();
                            } else {
                                unsubscribeLoader();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (hasView()) {
                                getView().showEmpty(true);
                            } else {
                                unsubscribeLoader();
                            }
                        }

                        @Override
                        public void onNext(List<String> strings) {
                            Timber.d("Paths = %s", Arrays.toString(strings.toArray()));
                            if (hasView()) {
                                if (!strings.isEmpty()) {
                                    getView().addAll(strings);
                                    getView().showList(true);
                                }
                            }
                        }
                    });
        }
    }

    void unsubscribeLoader() {
        if (loaderSub != null) {
            final Subscription s = loaderSub;
            final Scheduler.Worker worker = Schedulers.io().createWorker();
            worker.schedule(() -> {
                s.unsubscribe();
                worker.unsubscribe();
            });
            loaderSub = null;
        }
    }

    public void onOpenFolder(Context context, String path) {
        FolderPickerFragment f = FolderPickerFragment.ni(context, credentials, path);
        fm.replaceMainContent(f, true);
    }

    public void onFolderSelected(String path) {
        Intent i = new Intent().putExtra("path", path);
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, i);
    }

    public void createNewFolder() {
        dialogPresenter.showDialog(context -> {
            final EditText editText = new EditText(context);
            editText.setSingleLine(true);
            editText.setInputType(editText.getInputType()
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            return new AlertDialog.Builder(context)
                    .setTitle(R.string.new_folder)
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Editable e = editText.getText();
                            if (e != null) {
                                onFolderSelected(path + e.toString());
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        });
    }

    public ActionBarMenuHandler getToolbarConfig() {
        return ActionBarMenuConfig.builder()
                .withMenu(R.menu.new_folder)
                .setActionHandler((context, integer) -> {
                    switch (integer) {
                        case R.id.new_folder:
                            createNewFolder();
                            return true;
                        default:
                            return false;
                    }
                })
                .build();
    }

}
