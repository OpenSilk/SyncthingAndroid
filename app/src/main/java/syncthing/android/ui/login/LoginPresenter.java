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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.Bindable;
import android.databinding.PropertyChangeRegistry;
import android.databinding.adapters.ViewBindingAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.core.rx.RxUtils;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import mortar.ViewPresenter;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.settings.AppSettings;
import syncthing.android.ui.ManageActivity;
import syncthing.android.ui.binding.BindingSubscriptionsHolder;
import syncthing.api.Credentials;
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
public class LoginPresenter extends ViewPresenter<CoordinatorLayout> implements
        android.databinding.Observable, BindingSubscriptionsHolder {

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
    final ToolbarOwner toolbarOwner;

    final AtomicReference<Credentials> credentials = new AtomicReference<>();
    final SyncthingApiConfig.Builder configBuilder = SyncthingApiConfig.builder();
    final PropertyChangeRegistry registry = new PropertyChangeRegistry();

    String alias = "";
    String host = "";
    String port = "8384";
    String user = "";
    String pass = "";
    boolean tls;
    String errorHost;

    CompositeSubscription bindingSubscriptions;
    Subscription subscription;
    String error;
    Session session;
    State state = State.NONE;
    boolean wasPreviouslyLoaded;

    @Inject
    public LoginPresenter(
            @ForApplication Context context,
            Credentials initialCredentials,
            ActivityResultsController activityResultsController,
            SessionManager manager,
            AppSettings settings,
            DialogPresenter dialogPresenter,
            ToolbarOwner toolbarOwner
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
        this.toolbarOwner = toolbarOwner;
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
        if (!wasPreviouslyLoaded && savedInstanceState != null) {
            credentials.set(savedInstanceState.getParcelable("creds"));
        } else if (!wasPreviouslyLoaded) {
            if (credentials.get() != Credentials.NONE) {
                Credentials c = credentials.get();
                alias = c.alias;
                host = SyncthingUtils.extractHost(c.url);
                port = SyncthingUtils.extractPort(c.url);
                tls = SyncthingUtils.isHttps(c.url);
            }
        }
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

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        outState.putParcelable("creds", credentials.get());
    }

    @Override
    public CompositeSubscription bindingSubscriptions() {
        return (bindingSubscriptions != null) ? bindingSubscriptions : (bindingSubscriptions = new CompositeSubscription());
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        registry.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        registry.remove(callback);
    }

    private void notifyChange(int id) {
        registry.notifyChange(this, id);
    }

    public void submit(View btn) {
        dismissKeyboard(btn);
        if (isInputInvalid()) {
            dialogPresenter.showDialog(context -> new AlertDialog.Builder(context)
                    .setTitle(R.string.input_error)
                    .setMessage(R.string.input_error_message)
                    .setPositiveButton(android.R.string.cancel, null)
                    .setNegativeButton(R.string.save, (d, w) -> {
                        fetchApiKey();
                    })
                    .create());
        } else {
            fetchApiKey();
        }
    }

    public void cancel(View btn) {
        exitCanceled();
    }

    void fetchApiKey() {
        if (!hasView()) return;
        state = State.LOADING;
        showLoading();
        String url = SyncthingUtils.buildUrl(
                host, //TODO undocumented behavior (default port)
                StringUtils.isEmpty(port) ? (tls ? "443" : "80") : port,
                tls
        );
        Timber.d("Logging into %s", url);
        credentials.set(credentials.get().buildUpon().setUrl(url).build());
        configBuilder.setUrl(url);
        configBuilder.setAuth(SyncthingUtils.buildAuthorization(user, pass));
        configBuilder.setDebug(true);
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
                            if (StringUtils.isEmpty(alias)) {
                                for (DeviceConfig d : pair.first.devices) {
                                    if (StringUtils.equals(d.deviceID, pair.second.myID)) {
                                        builder.setAlias(SyncthingUtils.getDisplayName(d));
                                        break;
                                    }
                                }
                            } else {
                                builder.setAlias(alias);
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

    void dismissKeyboard(View v) {
        final InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    boolean isInputInvalid() {
        boolean invalid = false;
        invalid |= errorHost != null;
        return invalid;
    }

    @Bindable
    public String getAlias() {
        return alias;
    }

    public final Action1<CharSequence> actionSetAlias = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            String s = StringUtils.isEmpty(charSequence) ? "" : charSequence.toString();
            if (!StringUtils.equals(s, alias)) {
                alias = s;
            }
        }
    };

    @Bindable
    public String getHost() {
        return host;
    }

    @Bindable
    public String getErrorHost() {
        return errorHost;
    }

    public final Action1<CharSequence> actionSetHost = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            String s = StringUtils.isEmpty(charSequence) ? "" : charSequence.toString();
            boolean invalid = false;
            if (StringUtils.isEmpty(s)) {
                invalid = true;
            }
            if (hasView()) {
                String err = (invalid ? getView().getContext().getString(R.string.input_error) : null);
                if (!StringUtils.equals(err, errorHost)) {
                    errorHost = err;
                    notifyChange(syncthing.android.BR.errorHost);
                }
            }
            if (!invalid && !StringUtils.equals(s, host)) {
                host = s;
            }
        }
    };

    @Bindable
    public String getPort() {
        return port;
    }

    public final Action1<CharSequence> actionSetPort = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            String s = StringUtils.isEmpty(charSequence) ? "" : charSequence.toString();
            if (!StringUtils.equals(s, port)) {
                port = s;
            }
        }
    };

    @Bindable
    public String getUser() {
        return user;
    }

    public final Action1<CharSequence> actionSetUser = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            String s = StringUtils.isEmpty(charSequence) ? "" : charSequence.toString();
            if (!StringUtils.equals(s, user)) {
                user = s;
            }
        }
    };

    @Bindable
    public String getPass() {
        return pass;
    }

    public final Action1<CharSequence> actionSetPass = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            String s = StringUtils.isEmpty(charSequence) ? "" : charSequence.toString();
            if (!StringUtils.equals(s, pass)) {
                pass = s;
            }
        }
    };

    @Bindable
    public boolean isTls() {
        return tls;
    }

    public final Action1<Boolean> actionSetTls = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            tls = aBoolean;
        }
    };

    public final ViewBindingAdapter.OnViewAttachedToWindow toolbarAttachedListener =
            new ViewBindingAdapter.OnViewAttachedToWindow() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    Timber.d("attachingtoolbar");
                    Toolbar toolbar = (Toolbar)v;
                    toolbarOwner.attachToolbar(toolbar);
                    toolbarOwner.setConfig(ActionBarConfig.builder().setTitle(R.string.login).build());
                }
            };

    public final ViewBindingAdapter.OnViewDetachedFromWindow toolbarDetachedListener =
            new ViewBindingAdapter.OnViewDetachedFromWindow() {
                @Override
                public void onViewDetachedFromWindow(View v) {
                    Timber.d("detachingToolbar");
                    Toolbar toolbar = (Toolbar) v;
                    toolbarOwner.detachToolbar(toolbar);
                }
            };

    public final ViewBindingAdapter.OnViewDetachedFromWindow dropViewListener =
            new ViewBindingAdapter.OnViewDetachedFromWindow() {
                @Override
                public void onViewDetachedFromWindow(View v) {
                    Timber.d("Dropping view %s");
                    dropView((CoordinatorLayout) v);
                    RxUtils.unsubscribe(bindingSubscriptions);
                    bindingSubscriptions = null;
                }
            };

}
