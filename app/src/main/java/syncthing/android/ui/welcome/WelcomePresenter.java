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
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortarfragment.FragmentManagerOwner;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import mortar.ViewPresenter;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import syncthing.android.settings.AppSettings;
import syncthing.android.model.Credentials;
import syncthing.android.service.ConfigXml;
import syncthing.android.service.ServiceSettings;
import syncthing.android.service.SyncthingInstance;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.ManageActivity;
import syncthing.android.ui.login.LoginFragment;
import syncthing.android.ui.login.LoginUtils;
import syncthing.api.Session;
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

    final Context context;
    final AppSettings appSettings;
    final ActivityResultsController activityResultsController;
    final FragmentManagerOwner fragmentManager;
    final SessionManager manager;
    final ServiceSettings serviceSettings;

    int page;

    Subscription subscription;
    Subscription splashSubscription;
    Credentials newCredentials;
    Session session;
    final TempCredStorage tmpCreds = new TempCredStorage();
    final InitializedListener initializedListener = new InitializedListener(new Handler(Looper.getMainLooper()));
    Subscriber<Boolean> initializedSubscriber;
    final SyncthingApiConfig.Builder configBuilder = SyncthingApiConfig.builder();
    State state = State.NONE;

    static class TempCredStorage implements Serializable {
        private static final long serialVersionUID = 0L;
        String alias;
        String deviceId;
        String url;
        String key;
    }

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

    @Inject
    public WelcomePresenter(
            @ForApplication Context context,
            AppSettings appSettings,
            ActivityResultsController activityResultsController,
            FragmentManagerOwner fragmentManager,
            SessionManager manager,
            ServiceSettings serviceSettings
    ) {
        this.context = context;
        this.appSettings = appSettings;
        this.activityResultsController = activityResultsController;
        this.fragmentManager = fragmentManager;
        this.manager = manager;
        this.serviceSettings = serviceSettings;
        this.page = 0;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        context.getContentResolver().unregisterContentObserver(initializedListener);
        if (splashSubscription != null) {
            splashSubscription.unsubscribe();
        }
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        reload();
        delayHideSplash();
    }

    void reload() {
        if (!hasView())
            return;
        getView().setPage(page);
    }

    void updatePage(int page) {
        this.page = page;
        reload();
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

    //TODO this doesnt work retrofit observables seem to be a onshot deal
    public static Observable<?> retryObservable(Observable<?> attempts) {
        Timber.d("retryObservable(%s)", attempts);
        final int retries = 6*30; // Wait up to 30 minutes
        return attempts
                .zipWith(Observable.range(1, retries + 1),
                        (n, i) -> new Pair<Throwable, Integer>((Throwable)n, i))
                .flatMap(
                        pair -> {
                            if (pair.first != null) {
                                String msg = pair.first.getMessage();
                                if (msg != null &&
                                        !msg.contains("ECONNREFUSED") &&
                                        !msg.contains("EOFException") &&
                                        !msg.contains("timeout") &&
                                        !msg.contains("Connection reset by peer")) {
                                    // Another service running, propagate error and show to user
                                    Timber.d("Unknown error %s", msg);
                                    return Observable.error(pair.first);
                                }
                            }
                            if (pair.second > retries) {
                                Timber.d("Timeout waiting for daemon");
                                return Observable.error(new Exception("Timeout generating keys"));
                            }
                            Timber.d("Retrying it 10 seconds");
                            return Observable.timer((long) 10, TimeUnit.SECONDS);
                        });
    }

    void acquireNewSession() {
        if (session != null) {
            manager.release(session);
        }
        session = manager.acquire(configBuilder.build());
    }

    void onInstanceInitialized() {
        String alias = SyncthingUtils.generateDeviceName(false);
        tmpCreds.alias = alias;
        String url = "127.0.0.1";
        String port = "8385";
        String uri = LoginUtils.buildUri(url, port, true);
        tmpCreds.url = uri;
        configBuilder.setUrl(uri);
        acquireNewSession();
        final SyncthingApi api = SynchingApiWrapper.wrap(session.api(), Schedulers.io());
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
                            return new Pair<>(config, tmp);
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<Config, TempCredStorage>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        processError(e);
                    }

                    @Override
                    public void onNext(Pair<Config, TempCredStorage> configTempCredStoragePair) {
                        updateInstanceDefaults(configTempCredStoragePair);
                    }
                });
    }

    void updateInstanceDefaults(Pair<Config, TempCredStorage> configTempCredStoragePair) {

        TempCredStorage creds = configTempCredStoragePair.second;
        tmpCreds.alias =creds.alias;
        tmpCreds.deviceId = creds.deviceId;
        tmpCreds.key = creds.key;

        configBuilder.setApiKey(creds.key);

        String username = SyncthingUtils.generateUsername();
        String password = SyncthingUtils.generatePassword();
        Timber.i("Generated GUI username and password (" + username + ", " + password + ")");
        Config config = configTempCredStoragePair.first;
        config.gui.user = username;
        config.gui.password = password;

        acquireNewSession();
        final SyncthingApi api = SynchingApiWrapper.wrap(session.api(), Schedulers.io());
        //send new config
        subscription = api.updateConfig(config)
                //restart
                .flatMap(config1 -> api.restart())
                // Wait until Syncthing is ready
                .delay(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Ok>() {
                    @Override public void onCompleted() { }

                    @Override public void onError(Throwable e) {
                        processError(e);
                    }

                    @Override public void onNext(Ok ok) {
                        newCredentials = new Credentials(
                                tmpCreds.alias, tmpCreds.deviceId,
                                tmpCreds.url, tmpCreds.key,
                                SyncthingUtils.getSyncthingCACert(context));
                        appSettings.saveCredentials(newCredentials);
                        appSettings.setDefaultCredentials(newCredentials);
                        Timber.d(ReflectionToStringBuilder.reflectionToString(newCredentials));
                        finish(ok);
                    }
                });
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
        if (subscription != null) {
            final Subscription s = subscription;
            final Scheduler.Worker worker = Schedulers.io().createWorker();
            worker.schedule(() -> {
                s.unsubscribe();
                worker.unsubscribe();
            });
        }
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
        Timber.d("processError(%s)", t.getMessage());
        String msg = t.getMessage();
        if (appSettings.getSavedCredentials().size() > 0) {
            finish(null);
        } else if (msg != null && (msg.contains("ECONNREFUSED")
                || msg.contains("EOFException")
                || msg.contains("timeout")
                || msg.contains("Connection reset by peer"))) {
            Observable.timer(10, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .subscribe((i) -> onInstanceInitialized());
        } else if (msg != null && (msg.contains("Unauthorized")
                || msg.contains("Forbidden")
                || msg.contains("Untrusted Certificate"))) {
            ConfigXml configXml = ConfigXml.get(context);
            if (configXml != null) {
                configBuilder.setApiKey(configXml.getApiKey());
                configBuilder.setCaCert(SyncthingUtils.getSyncthingCACert(context));
                Observable.timer(10, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                        .subscribe((i) -> onInstanceInitialized());
            } else if (!serviceSettings.isInitialised()) {
                serviceSettings.setEnabled(false);
                throw new RuntimeException("Daemon failed to start");
            } else {
                notifyError(t);
            }
        } else {
            notifyError(t);
        }
    }

    void notifyError(Throwable t) {
        Timber.d("notifyError(%s)", t.getMessage());
        state = State.ERROR;
        if (hasView()) {
            getView().showError(t.getMessage());
        }
        if (initializedSubscriber != null) {
            initializedSubscriber.onNext(false);
        }
    }

    void exitSuccess() {
        Intent intent = new Intent()
                .putExtra(ManageActivity.EXTRA_CREDENTIALS, (Parcelable) newCredentials);
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, intent);
    }

    void exitCanceled() {
        fragmentManager.killBackStack();
        fragmentManager.replaceMainContent(LoginFragment.newInstance(), false);
    }

    public ServiceSettings getServiceSettings() {
        return serviceSettings;
    }

    public State getState() {
        return state;
    }

}
