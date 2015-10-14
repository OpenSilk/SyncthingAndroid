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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;

import java.io.Serializable;

import javax.inject.Inject;

import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import syncthing.android.AppSettings;
import syncthing.android.model.Credentials;
import syncthing.android.service.SyncthingUtils;
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

    final Context appContext;
    final Credentials initialCredentials;
    final ActivityResultsController activityResultsController;
    final AppSettings settings;
    final SessionManager manager;

    Subscription subscription;
    boolean isloading;
    Credentials newCredentials;
    String error;
    TempCredStorage tmpCreds = new TempCredStorage();
    Session session;
    SyncthingApiConfig.Builder configBuilder = SyncthingApiConfig.builder();

    static class TempCredStorage implements Serializable {
        private static final long serialVersionUID = 0L;
        String alias;
        String deviceId;
        String url;
        String key;
    }

    @Inject
    public LoginPresenter(
            @ForApplication Context context,
            Credentials initialCredentials,
            ActivityResultsController activityResultsController,
            SessionManager manager,
            AppSettings settings
    ) {
        this.appContext = context;
        this.initialCredentials = initialCredentials;
        this.activityResultsController = activityResultsController;
        this.settings = settings;
        this.manager = manager;
        if (initialCredentials != Credentials.NONE) {
            configBuilder.forCredentials(initialCredentials);
        }
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
            if (initialCredentials != Credentials.NONE) {
                getView().initWithCredentials(initialCredentials);
            }
        } else {
            savedInstanceState.setClassLoader(getClass().getClassLoader());
            isloading = savedInstanceState.getBoolean("isloading");
            newCredentials = savedInstanceState.getParcelable("creds");
            error = savedInstanceState.getString("error");
            tmpCreds = (TempCredStorage) savedInstanceState.getSerializable("tmpcreds");
            if (isloading) {
                getView().showLoginProgress();
            } else if (newCredentials != null) {
                exitSuccess();
            } else if (error != null) {
                getView().showLoginError(error);
            }
        }
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putBoolean("isloading", isloading);
        outState.putParcelable("creds", (Parcelable) newCredentials);
        outState.putString("error", error);
        outState.putSerializable("tmpcreds", tmpCreds);
    }

    void fetchApiKey(String alias, String url, String port, String user, String pass, boolean tls) {
        if (!hasView()) return;
        tmpCreds.alias = alias;
        String uri = LoginUtils.buildUri(url, port, tls);
        tmpCreds.url = uri;
        configBuilder.setUrl(uri);
        String auth = LoginUtils.buildAuthorization(user, pass);
        configBuilder.setAuth(auth);
        if (session != null) {
            manager.release(session);
        }
        session = manager.acquire(configBuilder.build());
        final SyncthingApi api = SynchingApiWrapper.wrap(session.api(), Schedulers.io());
        isloading = true;
        subscription = api.config()
                .zipWith(api.system(),
                (config, system) -> {
                    TempCredStorage tmp = new TempCredStorage();
                    tmp.key = config.gui.apiKey;
                    tmp.deviceId = system.myID;
                    for (DeviceConfig d : config.devices) {
                        if (StringUtils.equals(d.deviceID, system.myID)) {
                            tmp.alias = SyncthingUtils.getDisplayName(d);
                        }
                    }
                    Timber.d(ReflectionToStringBuilder.reflectionToString(tmp));
                    return tmp;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tmp -> {
                            tmpCreds.deviceId = tmp.deviceId;
                            tmpCreds.key = tmp.key;
                            if (StringUtils.isEmpty(tmpCreds.alias)) {
                                tmpCreds.alias = tmp.alias;
                            }
                            saveKeyAndFinish();
                        },
                        this::notifyLoginError
                );
    }

    void cancelLogin() {
        if (subscription != null) {
            final Subscription s = subscription;
            subscription = null;
            final Scheduler.Worker worker = Schedulers.io().createWorker();
            worker.schedule(new Action0() {
                @Override
                public void call() {
                    s.unsubscribe();
                    worker.unsubscribe();
                }
            });
        }
        getView().dismissLoginProgress();
    }

    void saveKeyAndFinish() {
        isloading = false;
        newCredentials = new Credentials(tmpCreds.alias, tmpCreds.deviceId, tmpCreds.url, tmpCreds.key, null);
        Timber.d(ReflectionToStringBuilder.reflectionToString(newCredentials));
        settings.saveCredentials(newCredentials);
        if (hasView()) {
            exitSuccess();
        }
    }

    void notifyLoginError(Throwable t) {
        isloading = false;
        error = t.getMessage();
        if (hasView()) {
            getView().dismissLoginProgress();
            getView().showLoginError(error);
        }
    }

    void exitSuccess() {
        Intent intent = new Intent().putExtra(ManageActivity.EXTRA_CREDENTIALS, (Parcelable) newCredentials);
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, intent);
    }

    void exitCanceled() {
        activityResultsController.setResultAndFinish(Activity.RESULT_CANCELED, null);
    }

    void gotoCredentialsInstaller() {
        Intent intent = new Intent("android.credentials.INSTALL")
                .setPackage("com.android.certinstaller");
                //.setComponent(new ComponentName(""com.android.certinstaller", ".CertInstallerMain"));
        activityResultsController.startActivityForResult(intent, 0, null);
    }

}
