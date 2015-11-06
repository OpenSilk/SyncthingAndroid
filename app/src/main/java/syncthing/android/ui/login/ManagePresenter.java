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
import android.os.Bundle;

import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortarfragment.FragmentManagerOwner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import mortar.ViewPresenter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import syncthing.android.settings.AppSettings;
import syncthing.android.identicon.IdenticonGenerator;
import syncthing.android.model.Credentials;
import timber.log.Timber;

/**
 * Created by drew on 3/15/15.
 */
@ScreenScope
public class ManagePresenter extends ViewPresenter<ManageScreenView> {

    final IdenticonGenerator identiconGenerator;
    final FragmentManagerOwner fragmentManagerOwner;
    final AppSettings appSettings;
    final ActivityResultsController activityResultsController;

    Subscription loaderSubscription;
    boolean adapterDirty;

    @Inject
    public ManagePresenter(
            IdenticonGenerator identiconGenerator,
            FragmentManagerOwner fragmentManagerOwner,
            AppSettings appSettings,
            ActivityResultsController activityResultsController
    ) {
        this.identiconGenerator = identiconGenerator;
        this.fragmentManagerOwner = fragmentManagerOwner;
        this.appSettings = appSettings;
        this.activityResultsController = activityResultsController;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        if (loaderSubscription != null) loaderSubscription.unsubscribe();
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (loaderSubscription == null || loaderSubscription.isUnsubscribed()) {
            adapterDirty = true;
            reload();
        }
    }

    void reload() {
        if (loaderSubscription != null) loaderSubscription.unsubscribe();
        loaderSubscription = Observable.create(new Observable.OnSubscribe<List<Credentials>>() {
            @Override
            public void call(Subscriber<? super List<Credentials>> subscriber) {
                subscriber.onNext(appSettings.getSavedCredentialsSorted());
                subscriber.onCompleted();
            }
        }).map(creds -> {
            List<ManageDeviceCard> cards = new ArrayList<>(creds.size());
            Credentials defaultCredentials = appSettings.getDefaultCredentials();
            for (Credentials c : creds) {
                ManageDeviceCard card = new ManageDeviceCard(ManagePresenter.this, c);
                card.setChecked(c.equals(defaultCredentials));
            }
            return cards;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ManageDeviceCard>>() {
                    @Override
                    public void onCompleted() {
                        if (hasView()) {
                            getView().onComplete();
                        } else if (loaderSubscription != null) {
                            loaderSubscription.unsubscribe();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "loadCredentials");
                        //TODO notify
                        if (loaderSubscription != null) {
                            loaderSubscription.unsubscribe();
                        }
                    }

                    @Override
                    public void onNext(List<ManageDeviceCard> manageDeviceCards) {
                        if (hasView()) {
                            getView().addAll(manageDeviceCards, adapterDirty);
                            adapterDirty = false;
                        }
                    }
                });
    }

    void openAddScreen() {
        LoginFragment f = LoginFragment.newInstance(Credentials.NONE);
        fragmentManagerOwner.replaceMainContent(f, true);
    }

    void openEditScreen(Credentials credentials) {
        LoginFragment f = LoginFragment.newInstance(credentials);
        fragmentManagerOwner.replaceMainContent(f, true);
    }

    void removeDevice(Credentials credentials) {
        appSettings.removeCredentials(credentials);
        adapterDirty = true;
        reload();
    }

    void exitActivity() {
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, null);
    }

    void setAsDefault(Credentials credentials) {
        appSettings.setDefaultCredentials(credentials);
        adapterDirty = true;
        reload();
    }

}
