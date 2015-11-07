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

package syncthing.android.ui.welcome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortarfragment.FragmentManagerOwner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import mortar.ViewPresenter;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.observers.Subscribers;
import rx.schedulers.Schedulers;
import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.android.service.ConfigXml;
import syncthing.android.service.ServiceSettings;
import syncthing.android.service.SyncthingInstance;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.settings.AppSettings;
import syncthing.android.ui.ManageActivity;
import syncthing.android.ui.login.LoginFragment;
import syncthing.android.ui.login.LoginUtils;
import syncthing.api.SessionManager;
import syncthing.api.SynchingApiWrapper;
import syncthing.api.SyncthingApi;
import syncthing.api.SyncthingApiConfig;
import syncthing.api.model.Config;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.Ok;
import timber.log.Timber;

@ScreenScope
public class WelcomePresenter extends ViewPresenter<WelcomeScreenView>{

    class InitializedListener extends ContentObserver {
        public InitializedListener(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onInstanceInitialized();
        }
    }

    enum State {
        NONE,
        GENERATING,
        SUCCESS,
        ERROR,
    }

    final Context context;
    final AppSettings appSettings;
    final ActivityResultsController activityResultsController;
    final FragmentManagerOwner fragmentManager;
    final SessionManager manager;
    final ServiceSettings serviceSettings;
    final DialogPresenter dialogPresenter;

    final InitializedListener initializedListener = new InitializedListener(new Handler(Looper.getMainLooper()));
    final SyncthingApiConfig.Builder configBuilder = SyncthingApiConfig.builder();
    final AtomicReference<Credentials> credentials = new AtomicReference<>();

    int page = 0;
    State state = State.NONE;
    String error;

    Subscription subscription;
    Subscription splashSubscription;
    Subscriber<Boolean> initializedSubscriber;

    @Inject
    public WelcomePresenter(
            @ForApplication Context context,
            AppSettings appSettings,
            ActivityResultsController activityResultsController,
            FragmentManagerOwner fragmentManager,
            SessionManager manager,
            ServiceSettings serviceSettings,
            DialogPresenter dialogPresenter
    ) {
        this.context = context;
        this.appSettings = appSettings;
        this.activityResultsController = activityResultsController;
        this.fragmentManager = fragmentManager;
        this.manager = manager;
        this.serviceSettings = serviceSettings;
        this.dialogPresenter = dialogPresenter;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        context.getContentResolver().unregisterContentObserver(initializedListener);
        if (splashSubscription != null) {
            splashSubscription.unsubscribe();
        }
        unsubscribeS();
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        switch (state) {
            case NONE:
                delayHideSplash();
                break;
            case SUCCESS:
                exitSuccess();
                break;
            case ERROR:
                showError();
                break;
        }
    }

    void reload() {
        if (!hasView())
            return;
        getView().setPage(page);
    }

    void updatePage(int page) {
        this.page = page;
    }

    void delayHideSplash() {
        splashSubscription = Observable.timer(10, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(subscriber -> hideSplash());
    }

    void hideSplash() {
        if (page != 0)
            return;
        if (!hasView())
            return;
        getView().hideSplash();
    }

    void unsubscribeS() {
        if (subscription != null) {
            final Subscription s = subscription;
            final Scheduler.Worker worker = Schedulers.io().createWorker();
            worker.schedule(() -> {
                s.unsubscribe();
                worker.unsubscribe();
            });
        }
    }

    /*
     * Im sure your looking at this and thinking for the love of god why
     * We use this rather complicated observable to fetch our credential information
     * and save a generated password all in one fail swoop.
     * The observable handles acquiring and releasing the session, (note we must acquire
     * two sessions so the post will succeed), the observable also handles retries.
     * By using 'defer' we create new OkHttp Calls on each retry, this is mandatory as
     * calls are oneshot deals.
     */
    void onInstanceInitialized() {
        configBuilder.setUrl(LoginUtils.buildUrl(ConfigXml.LOCALHOST_IP, ConfigXml.DEFAULT_PORT, true));
        subscription = Observable.using(
                () -> {
                    Timber.d("Acquiring session");
                    return manager.acquire(configBuilder.build());
                },
                s -> {
                    final SyncthingApi api = SynchingApiWrapper.wrap(s.api(), Schedulers.io());
                    return Observable.defer(() -> Observable.zip(api.system(), api.config(), (systemInfo, config) -> {
                        //build our localhost credentials
                        Credentials.Builder builder = Credentials.builder();
                        builder.setUrl(LoginUtils.buildUrl(ConfigXml.LOCALHOST_IP,
                                ConfigXml.DEFAULT_PORT, true));
                        builder.setApiKey(config.gui.apiKey);
                        builder.setId(systemInfo.myID);
                        for (DeviceConfig d : config.devices) {
                            if (StringUtils.equals(d.deviceID, systemInfo.myID)) {
                                builder.setAlias(SyncthingUtils.getDisplayName(d));
                                break;
                            }
                        }
                        builder.setCaCert(SyncthingUtils.getSyncthingCACert(context));

                        //save and set as default
                        Credentials c = builder.build();
                        credentials.set(c);
                        appSettings.saveCredentials(c);
                        appSettings.setDefaultCredentials(c);

                        //create random user/pass
                        String username = SyncthingUtils.generateUsername();
                        String password = SyncthingUtils.generatePassword();
                        Timber.i("Generated GUI username and password (%s, %s)", username, password);
                        config.gui.user = username;
                        config.gui.password = password;

                        return config;
                    })).retryWhen(observable -> observable.flatMap(throwable -> {
                        String msg = throwable.getMessage();
                        if (msg == null ||
                                !(msg.contains("ECONNREFUSED") ||
                                msg.contains("EOFException") ||
                                msg.contains("timeout") ||
                                msg.contains("Connection reset by peer"))) {
                            // Another service running, propagate error and show to user
                            Timber.d("Unknown error %s", msg);
                            return Observable.error(throwable);
                        }
                        Timber.d("Retrying in 5 seconds");
                        return Observable.timer(5, TimeUnit.SECONDS);
                    }));
                },
                s -> {
                    Timber.d("Releasing session");
                    manager.release(s);
                },
                true // eagerly release
        ).observeOn(AndroidSchedulers.mainThread()
        ).flatMap(this::saveConfigObservable //want subscribe on main thread
        ).observeOn(AndroidSchedulers.mainThread()
        ).subscribe(Subscribers.create(this::finish, this::processError) //want results on main thread
        );
    }

    Observable<Ok> saveConfigObservable(final Config config) {
        return Observable.using(
                () -> {
                    Timber.d("Acquiring session2");
                    configBuilder.setApiKey(credentials.get().apiKey);
                    configBuilder.setCaCert(credentials.get().caCert);
                    return manager.acquire(configBuilder.build());
                },
                s -> {
                    final SyncthingApi api = SynchingApiWrapper.wrap(s.api(), Schedulers.io());
                    return Observable.defer(() -> api.updateConfig(config)
                            //restart
                            .flatMap(c -> api.restart())
                            // Give syncthing a chance to reboot
                            .delay(10, TimeUnit.SECONDS)
                    );
                },
                s -> {
                    Timber.d("Releasing session2");
                    manager.release(s);
                },
                true // eagerly release
        );//TODO what kind of errors can be recovered from at this point
    }

    public void initializeInstance() {
        state = State.GENERATING;
        serviceSettings.setEnabled(true);
        context.startService(new Intent(context, SyncthingInstance.class).setAction(SyncthingInstance.REEVALUATE));
        context.getContentResolver().registerContentObserver(serviceSettings.getInitializedUri(), false, initializedListener);
    }

    public void setInitializedSubscriber(Subscriber<Boolean> subscription) {
        initializedSubscriber = subscription;
    }

    void cancelGeneration() {
        unsubscribeS();
        exitCanceled();
    }

    void finish(Ok ok) {
        state = State.SUCCESS;
        Timber.d("Ready");
        if (page == 1)
            this.page = 5;
        if (initializedSubscriber != null) {
            initializedSubscriber.onNext(true);
        }
        reload();
    }

    void processError(Throwable t) {
        Timber.w("processError(%s)", t.getMessage());
        if (!serviceSettings.isInitialised()) {
            serviceSettings.setEnabled(false);
            context.stopService(new Intent(context, SyncthingInstance.class));
            throw new RuntimeException("Daemon failed to start");
        } else {
            notifyError(t);
        }
    }

    void notifyError(Throwable t) {
        Timber.d("notifyError(%s)", t.getMessage());
        state = State.ERROR;
        error = t.getMessage();
        showError();
        if (initializedSubscriber != null) {
            initializedSubscriber.onNext(false);
        }
    }

    void showError() {
        if (hasView()) {
            dialogPresenter.showDialog(context -> new AlertDialog.Builder(context)
                    .setTitle(R.string.welcome_pl_generating_failed)
                    .setMessage(error)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
            );
        }
    }

    void exitSuccess() {
        Intent intent = new Intent().putExtra(ManageActivity.EXTRA_CREDENTIALS, credentials.get());
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, intent);
    }

    void exitCanceled() {
        fragmentManager.killBackStack();
        fragmentManager.replaceMainContent(LoginFragment.newInstance(), false);
    }

    public State getState() {
        return state;
    }

}
