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

package syncthing.android.ui.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import syncthing.android.R;
import syncthing.api.Credentials;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.settings.AppSettings;
import syncthing.android.ui.ManageActivity;
import syncthing.api.Session;
import syncthing.api.SessionManager;
import syncthing.api.SynchingApiWrapper;
import syncthing.api.SyncthingApi;
import syncthing.api.SyncthingApiConfig;
import syncthing.api.model.DeviceConfig;
import timber.log.Timber;

/**
* Created by drew on 3/11/15.
*/
@ScreenScope
public class LoginPresenter extends ViewPresenter<LoginScreenView> {

    enum State {
        NONE,
        LOADING,
        ERROR,
        SUCCESS
    }

    final Context appContext;
    final ActivityResultsController activityResultsController;
    final AppSettings settings;
    final SessionManager manager;
    final DialogPresenter dialogPresenter;

    final LoginScreenViewModel viewModel = new LoginScreenViewModel();
    final AtomicReference<Credentials> credentials = new AtomicReference<>();
    final SyncthingApiConfig.Builder configBuilder = SyncthingApiConfig.builder();

    Subscription subscription;
    String error;
    Session session;
    State state = State.NONE;

    @Inject
    public LoginPresenter(
            @ForApplication Context context,
            Credentials initialCredentials,
            ActivityResultsController activityResultsController,
            SessionManager manager,
            AppSettings settings,
            DialogPresenter dialogPresenter
    ) {
        this.appContext = context;
        this.credentials.set(initialCredentials);
        this.activityResultsController = activityResultsController;
        this.settings = settings;
        this.manager = manager;
        if (initialCredentials != Credentials.NONE) {
            configBuilder.forCredentials(initialCredentials);
        }
        this.dialogPresenter = dialogPresenter;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        Timber.d("onEnterScope");
        super.onEnterScope(scope);
    }

    @Override
    protected void onExitScope() {
        Timber.d("onExitScope");
        super.onExitScope();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        if (session != null) {
            manager.release(session);
        }
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (savedInstanceState == null) {
            if (credentials.get() != Credentials.NONE) {
                Credentials c = credentials.get();
                viewModel.setAlias(c.alias);
                viewModel.setHost(LoginUtils.extractHost(c.url));
                viewModel.setPort(LoginUtils.extractPort(c.url));
                viewModel.setTls(LoginUtils.isHttps(c.url));
            }
        }
        getView().binding.setModel(viewModel);
        switch (state) {
            case ERROR:
                showError();
                break;
            case LOADING:
                showLoading();
                break;
            case SUCCESS:
                exitSuccess();
                break;
        }
    }

    public void submit(View btn) {
        if (!hasView()) return;
        LoginScreenView v = getView();
        if (LoginUtils.validateHost(viewModel.getHost())
                && LoginUtils.validatePort(viewModel.getPort())) {
            v.dismissKeyboard();
            fetchApiKey();
        } else {
            Toast.makeText(v.getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void cancel(View btn) {
        exitCanceled();
    }

    void fetchApiKey() {
        if (!hasView()) return;
        state = State.LOADING;
        showLoading();
        String url = LoginUtils.buildUrl(viewModel.getHost(), viewModel.getPort(), viewModel.isTls());
        credentials.set(credentials.get().buildUpon().setUrl(url).build());
        configBuilder.setUrl(url);
        configBuilder.setAuth(LoginUtils.buildAuthorization(viewModel.getUser(), viewModel.getPass()));
        if (session != null) {
            manager.release(session);
        }
        session = manager.acquire(configBuilder.build());
        final SyncthingApi api = SynchingApiWrapper.wrap(session.api(), Schedulers.io());
        subscription = rx.Observable.zip(api.config(), api.system(), Pair::create)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> {
                            Credentials.Builder builder = credentials.get().buildUpon();
                            builder.setId(pair.second.myID);
                            builder.setApiKey(pair.first.gui.apiKey);
                            if (StringUtils.isEmpty(viewModel.getAlias())) {
                                for (DeviceConfig d : pair.first.devices) {
                                    if (StringUtils.equals(d.deviceID, pair.second.myID)) {
                                        builder.setAlias(SyncthingUtils.getDisplayName(d));
                                        break;
                                    }
                                }
                            } else {
                                builder.setAlias(viewModel.getAlias());
                            }
                            credentials.set(builder.build());
                        },
                        this::notifyLoginError,
                        this::saveKeyAndFinish
        );
    }

    void notifyLoginError(Throwable t) {
        state = State.ERROR;
        error = t.getMessage();
        if (hasView()) {
            showError();
        }
    }

    void saveKeyAndFinish() {
        state = State.SUCCESS;
        settings.saveCredentials(credentials.get());
        if (hasView()) {
            exitSuccess();
        }
    }

    void cancelLogin() {
        if (subscription != null) {
            final Subscription s = subscription;
            subscription = null;
            final Scheduler.Worker worker = Schedulers.io().createWorker();
            worker.schedule(() -> {
                s.unsubscribe();
                worker.unsubscribe();
            });
        }
    }

    void showLoading() {
        dialogPresenter.showDialog(
                context -> {
                    ProgressDialog loadingProgress = new ProgressDialog(context);
                    loadingProgress.setMessage(context.getString(R.string.fetching_api_key_dots));
                    loadingProgress.setCancelable(false);
                    loadingProgress.setButton(DialogInterface.BUTTON_NEGATIVE,
                            context.getString(android.R.string.cancel),
                            (dialog, which) -> {
                                cancelLogin();
                                dialog.dismiss();
                            });
                    return loadingProgress;
                }
        );
    }

    void showError() {
        dialogPresenter.showDialog(
                context -> new AlertDialog.Builder(context)
                        .setTitle(R.string.login_failure)
                        .setMessage(error)
                        .setPositiveButton(android.R.string.ok, null)
                        .create());
    }

    void exitSuccess() {
        Intent intent = new Intent().putExtra(ManageActivity.EXTRA_CREDENTIALS, (Parcelable) credentials.get());
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, intent);
    }

    public void exitCanceled() {
        activityResultsController.setResultAndFinish(Activity.RESULT_CANCELED, new Intent());
    }

}
