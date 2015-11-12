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
import android.app.ProgressDialog;
import android.databinding.PropertyChangeRegistry;
import android.databinding.adapters.ViewBindingAdapter;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.opensilk.common.core.rx.RxUtils;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import syncthing.android.R;
import syncthing.android.ui.binding.BindingSubscriptionsHolder;
import syncthing.api.Credentials;
import syncthing.api.Session;
import syncthing.api.SessionController;
import syncthing.api.SessionManager;
import timber.log.Timber;

/**
 * Created by drew on 3/23/15.
 */
public class EditPresenter<V extends View> extends ViewPresenter<V> implements android.databinding.Observable, BindingSubscriptionsHolder {

    protected final SessionManager manager;
    protected final SessionController controller;
    protected final String folderId;
    protected final String deviceId;
    protected final boolean isAdd;
    protected final Session session;
    protected final DialogPresenter dialogPresenter;
    protected final ActivityResultsController activityResultsController;
    protected final Credentials credentials;
    protected final ToolbarOwner toolbarOwner;

    protected final PropertyChangeRegistry registry = new PropertyChangeRegistry();
    protected CompositeSubscription bindingSubscriptions;
    protected Subscription saveSubscription;
    protected int titleRes;
    protected boolean wasPreviouslyLoaded;

    public EditPresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            ToolbarOwner toolbarOwner,
            EditPresenterConfig config
    ) {
        this.manager = manager;
        this.dialogPresenter = dialogPresenter;
        this.activityResultsController = activityResultContoller;
        this.toolbarOwner = toolbarOwner;
        this.session = manager.acquire(config.credentials);
        this.controller = this.session.controller();
        this.folderId = config.folderId;
        this.deviceId = config.deviceId;
        this.isAdd = config.isAdd;
        this.credentials = config.credentials;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        titleRes = EditFragment2.TitleService.getTitle(scope);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        unsubscribe(saveSubscription);
        manager.release(session);
    }

    //TODO save saving state and restore

    protected void onSaveStart() {
        dialogPresenter.showDialog(
                context -> {
                    ProgressDialog mProgressDialog = new ProgressDialog(context);
                    mProgressDialog.setMessage(context.getResources().getString(R.string.saving_config_dots));
                    return mProgressDialog;
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
        dialogPresenter.showDialog(context -> new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(msg)
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
        );
    }

    public void dismissDialog(View btn) {
        dismissDialog();
    }

    protected void dismissDialog() {
        activityResultsController.setResultAndFinish(Activity.RESULT_OK, null);
    }

    protected void unsubscribe(final Subscription s) {
        if (s != null) {
            final Scheduler.Worker worker = Schedulers.io().createWorker();
            worker.schedule(() -> {
                s.unsubscribe();
                worker.unsubscribe();
            });
        }
    }

    public ActionBarConfig getToolbarConfig() {
        return ActionBarConfig.builder()
                .setTitle(titleRes).build();
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        registry.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        registry.remove(callback);
    }

    protected void notifyChange(int val) {
        registry.notifyChange(this, val);
    }

    @Override
    public CompositeSubscription bindingSubscriptions() {
        return (bindingSubscriptions != null) ? bindingSubscriptions : (bindingSubscriptions = new CompositeSubscription());
    }

    public final ViewBindingAdapter.OnViewAttachedToWindow toolbarAttachedListener =
            new ViewBindingAdapter.OnViewAttachedToWindow() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    Toolbar toolbar = (Toolbar)v;
                    toolbarOwner.attachToolbar(toolbar);
                    toolbarOwner.setConfig(getToolbarConfig());
                }
            };

    public final ViewBindingAdapter.OnViewDetachedFromWindow toolbarDetachedListener =
            new ViewBindingAdapter.OnViewDetachedFromWindow() {
                @Override
                public void onViewDetachedFromWindow(View v) {
                    Toolbar toolbar = (Toolbar) v;
                    toolbarOwner.detachToolbar(toolbar);
                }
            };

    public final ViewBindingAdapter.OnViewDetachedFromWindow dropViewListener =
            new ViewBindingAdapter.OnViewDetachedFromWindow() {
                @Override @SuppressWarnings("unchecked")
                public void onViewDetachedFromWindow(View v) {
                    Timber.d("Dropping view %s");
                    dropView((V) v);
                    RxUtils.unsubscribe(bindingSubscriptions);
                    bindingSubscriptions = null;
                }
            };
}
