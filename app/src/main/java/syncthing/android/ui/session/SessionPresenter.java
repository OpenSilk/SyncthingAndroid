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

package syncthing.android.ui.session;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortar.Lifecycle;
import org.opensilk.common.ui.mortar.LifecycleService;
import org.opensilk.common.ui.mortarfragment.FragmentManagerOwner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import mortar.MortarScope;
import mortar.Presenter;
import mortar.bundler.BundleService;
import rx.Subscription;
import rx.functions.Action1;
import syncthing.android.R;
import syncthing.android.identicon.IdenticonComponent;
import syncthing.android.identicon.IdenticonGenerator;
import syncthing.android.model.Credentials;
import syncthing.android.ui.ManageActivity;
import syncthing.android.ui.common.ActivityRequestCodes;
import syncthing.android.ui.sessionsettings.EditDeviceFragment;
import syncthing.android.ui.sessionsettings.EditFolderFragment;
import syncthing.android.ui.sessionsettings.EditIgnoresFragment;
import syncthing.android.ui.sessionsettings.SettingsFragment;
import syncthing.api.Session;
import syncthing.api.SessionController;
import syncthing.api.SessionManager;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.DeviceStats;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.SystemMessage;
import syncthing.api.model.Model;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;
import syncthing.api.model.event.DeviceRejected;
import syncthing.api.model.event.FolderCompletion;
import syncthing.api.model.event.FolderRejected;
import syncthing.api.model.event.FolderScanProgress;
import syncthing.api.model.event.FolderSummary;
import syncthing.api.model.event.StateChanged;
import timber.log.Timber;

/**
* Created by drew on 3/11/15.
*/
@ScreenScope
public class SessionPresenter extends Presenter<ISessionScreenView> implements
        android.databinding.DataBindingComponent, IdenticonComponent {

    final Context appContext;
    final Credentials credentials;
    final SessionController controller;
    final FragmentManagerOwner fragmentManagerOwner;
    final IdenticonGenerator identiconGenerator;
    final ActivityResultsController activityResultsController;
    final Session session;
    final SessionManager manager;
    final DialogPresenter dialogPresenter;

    Subscription changeSubscription;
    Subscription lifecycleSubscription;

    @Inject
    public SessionPresenter(
            @ForApplication Context appContext,
            Credentials credentials,
            SessionManager manager,
            FragmentManagerOwner fragmentManagerOwner,
            IdenticonGenerator identiconGenerator,
            ActivityResultsController activityResultsController,
            DialogPresenter dialogPresenter
    ) {
        this.appContext = appContext;
        this.credentials = credentials;
        this.fragmentManagerOwner = fragmentManagerOwner;
        this.identiconGenerator = identiconGenerator;
        this.activityResultsController = activityResultsController;
        this.session = manager.acquire(credentials);
        this.manager = manager;
        this.controller = session.controller();
        this.dialogPresenter = dialogPresenter;
    }

    @Override
    protected BundleService extractBundleService(ISessionScreenView view) {
        return BundleService.getBundleService(view.getContext());
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        Timber.d("onEnterScope");
        super.onEnterScope(scope);
        changeSubscription = controller.subscribeChanges(this::onChange);
        lifecycleSubscription = LifecycleService.getLifecycle(scope)
                .subscribe(new Action1<Lifecycle>() {
                    @Override
                    public void call(Lifecycle lifecycle) {
                        switch (lifecycle) {
                            case RESUME:
                                Timber.d("Initializing controller");
                                controller.init();
                                break;
                            case PAUSE:
                                Timber.d("Suspending controller");
                                controller.suspend();
                                break;
                        }
                    }
                });
    }

    @Override
    protected void onExitScope() {
        Timber.d("onExitScope");
        super.onExitScope();
        if (changeSubscription != null) {
            changeSubscription.unsubscribe();
        }
        if (lifecycleSubscription != null) {
            lifecycleSubscription.unsubscribe();
        }
        manager.release(session);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (controller.isOnline()) {
            initializeView();
            getView().showList(false);
            dismissRestartingDialog();
        } else if (controller.isRestarting()) {
            showRestartingDialog();
        } else /*offline*/ {
            getView().showLoading();
        }
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
    }

    void onChange(SessionController.ChangeEvent e) {
        SessionController.Change change = e.change;
        Timber.d("onChange(%s)", change.toString());
        switch (change) {
            case ONLINE:
                if (hasView()) {
                    initializeView();
                    getView().showList(true);
                    dismissRestartingDialog();
                    getView().updateToolbarState(true);
                }
                break;
            case OFFLINE:
                if (hasView()) {
                    if (controller.isRestarting()) {
                        showRestartingDialog();
                    } else {
                        getView().showLoading();
                    }
                    getView().updateToolbarState(false);
                }
                break;
            case FAILURE:
                if (hasView()) {
                    //controller has given up
                    getView().showEmpty(true);
                    dismissRestartingDialog();
                }
                break;
            case DEVICE_REJECTED:
            case FOLDER_REJECTED:
            case NOTICE:
                if (hasView()) {
                    List<NotifCard> notifications = updateNotifications(getView().getNotifications());
                    getView().refreshNotifications(notifications);
                }
                break;
            case CONFIG_UPDATE:
                if (hasView()) {
                    if (!controller.isConfigInSync()) {
                        getView().refreshNotifications(updateNotifications(getView().getNotifications()));
                    }
                    getView().refreshFolders(updateFolders(getView().getFolders()));
                    getView().refreshDevices(updateDevices(getView().getDevices()));
                    getView().refreshThisDevice(updateThisDevice(getView().getThisDevice()));
                }
                break;
            case NEED_LOGIN:
                openLoginScreen();
                break;
            case COMPLETION:
                if (hasView()) {
                    onCompletionUpdate(e.data, getView().getDevices());
                }
                break;
            case CONNECTIONS_UPDATE:
            case CONNECTIONS_CHANGE:
            case DEVICE_PAUSED:
            case DEVICE_RESUMED:
                if (hasView()) {
                    postConnectiosUpdate(getView().getThisDevice(), getView().getDevices());
                }
                break;
            case DEVICE_STATS:
                if (hasView()) {
                    postDeviceStatsUpdate(getView().getDevices());
                }
                break;
            case SYSTEM:
                if (hasView()) {
                    onSystemInfoUpdate(getView().getThisDevice());
                }
                break;
            case FOLDER_SUMMARY:
                if (hasView()) {
                    onFolderModelUpdate(e.data, getView().getFolders());
                }
                break;
            case STATE_CHANGED:
                if (hasView()) {
                    onFolderStateChange(e.data, getView().getFolders());
                }
                break;
            case FOLDER_STATS:
                Timber.w("Ignoring FOLDER_STATS update");
                break;
            case FOLDER_SCAN_PROGRESS: {
                if (hasView()) {
                    FolderScanProgress.Data d = (FolderScanProgress.Data) e.data;
                    FolderCard c = getFolderCard(d.folder, getView().getFolders());
                    if (c != null) {
                        c.setScanProgress(d.current, d.total);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    void initializeView() {
        if (!hasView()) throw new IllegalStateException("initialize called without view");
        List<NotifCard> notifications = updateNotifications(Collections.emptyList());
        List<FolderCard> folders = updateFolders(Collections.emptyList());
        MyDeviceCard myDevice = updateThisDevice(null);
        List<DeviceCard> devices = updateDevices(Collections.emptyList());
        getView().initialize(
                notifications,
                folders,
                myDevice,
                devices
        );
    }

    List<NotifCard> updateNotifications(List<NotifCard> notifications) {
        /*todo compare device/foler rej and update
        if (!controller.isConfigInSync()) {
            if (notifications.indexOf(NotifCardRestart.INSTANCE) == -1) {
                notifications.add(0, NotifCardRestart.INSTANCE);
            }
        } else {
            int idx;
            if ((idx = notifications.indexOf(NotifCardRestart.INSTANCE)) != -1) {
                notifications.remove(idx);
            }
        }
        GuiError guiError = controller.getLatestError();
        if (guiError != null) {
            NotifCardError.INSTANCE.setError(guiError);
            if (notifications.indexOf(NotifCardError.INSTANCE) == -1) {
                if (notifications.size() >= 1) {
                    //we want to be second
                    notifications.add(1, NotifCardError.INSTANCE);
                } else {
                    notifications.add(NotifCardError.INSTANCE);
                }
            }
        } else {
            int idx;
            if ((idx = notifications.indexOf(NotifCardError.INSTANCE)) != -1) {
                notifications.remove(idx);
            }
        }
        */
        notifications.clear();
        if (!controller.isConfigInSync()) {
            notifications.add(new NotifCardRestart(this));
        }
        SystemMessage guiError = controller.getLatestError();
        if (guiError != null) {
            NotifCardError errCard = new NotifCardError(this);
            errCard.setError(guiError);
            notifications.add(errCard);
        }
        for (Map.Entry<String, DeviceRejected> e : controller.getDeviceRejections()) {
            notifications.add(new NotifCardRejDevice(this, e.getKey(), e.getValue()));
        }
        for (Map.Entry<String, FolderRejected> e : controller.getFolderRejections()) {
            notifications.add(new NotifCardRejFolder(this, e.getKey(), e.getValue()));
        }
        return notifications;
    }

    List<FolderCard> updateFolders(List<FolderCard> folders) {
        Collection<FolderConfig> folderConfigs = controller.getFolders();
        if (folderConfigs.size() > 0) {
            Set<String> configIds = new HashSet<>(folderConfigs.size());
            for (FolderConfig c : folderConfigs) {
                configIds.add(c.id);
            }
            //remove any old ones
            Iterator<FolderCard> ic = folders.iterator();
            while (ic.hasNext()) {
                if(!configIds.contains(ic.next().getId())) {
                    ic.remove();
                }
            }
        }
        List<String> needsUpdate = new ArrayList<>();
        for (FolderConfig folder : folderConfigs) {
            Model model = controller.getModel(folder.id);
            FolderScanProgress.Data scanP = controller.getFolderScanProgress(folder.id);
            FolderCard card = getFolderCard(folder.id, folders);
            if (card != null && model != null) {
                card.setFolder(folder);
                card.setModel(model);
                if (scanP != null) {
                    card.setScanProgress(scanP.current, scanP.total);
                }
            } else if (card == null) {
                card = new FolderCard(this, folder, model);
                if (scanP != null) {
                    card.setScanProgress(scanP.current, scanP.total);
                }
                folders.add(card);
            }
            if (model == null) {
                needsUpdate.add(folder.id);
            }
        }
        if (!needsUpdate.isEmpty()) {
            controller.refreshFolders(needsUpdate);
        }
        Collections.sort(folders, (lhs, rhs) -> lhs.getId().compareTo(rhs.getId()));
        return folders;
    }

    private FolderCard getFolderCard(String id, List<FolderCard> folders) {
        for (FolderCard fc : folders) {
            if (StringUtils.equals(fc.getId(), id)) {
                return fc;
            }
        }
        return null;
    }

    MyDeviceCard updateThisDevice(MyDeviceCard myDevice) {
        DeviceConfig device = controller.getThisDevice();
        ConnectionInfo conn = controller.getConnectionTotal();
        SystemInfo sys = controller.getSystemInfo();
        Version ver = controller.getVersion();
        if (myDevice == null) {
            myDevice = new MyDeviceCard(device, conn, sys, ver);
        } else {
            myDevice.setConnectionInfo(conn);
            myDevice.setSystemInfo(sys);
            myDevice.setVersion(ver);
        }
        return myDevice;
    }

    List<DeviceCard> updateDevices(List<DeviceCard> devices) {
        Collection<DeviceConfig> remoteDevices = controller.getRemoteDevices();
        if (devices.size() >0 ) {
            Set<String> deviceIds = new HashSet<>(remoteDevices.size());
            for (DeviceConfig c : remoteDevices) {
                deviceIds.add(c.deviceID);
            }
            //remove any old ones
            Iterator<DeviceCard> ic = devices.iterator();
            while (ic.hasNext()) {
                if (!deviceIds.contains(ic.next().getDeviceID())) {
                    ic.remove();
                }
            }
        }
        for (DeviceConfig device : remoteDevices) {
            ConnectionInfo connection = controller.getConnection(device.deviceID);
            DeviceStats stats = controller.getDeviceStats(device.deviceID);
            int completion = controller.getCompletionTotal(device.deviceID);
            DeviceCard c = getDeviceCard(device.deviceID, devices);
            if (c != null) {
                c.setDevice(device);
                c.setConnectionInfo(connection);
                c.setDeviceStats(stats);
                c.setCompletion(completion);
            } else {
                devices.add(new DeviceCard(this, device, connection, stats, completion));
            }
        }
        Collections.sort(devices, (lhs, rhs) -> lhs.getDeviceID().compareTo(rhs.getDeviceID()));
        return devices;
    }

    private DeviceCard getDeviceCard(String id, List<DeviceCard> devices) {
        for (DeviceCard c : devices) {
            if (StringUtils.equals(c.device.deviceID, id)) {
                return c;
            }
        }
        return null;
    }

    void onCompletionUpdate(Object o, List<DeviceCard> devices) {
        if (SessionController.ChangeEvent.NONE == o) {
            for (DeviceCard c : devices) {
                c.setCompletion(controller.getCompletionTotal(c.getDeviceID()));
            }
        } else {
            FolderCompletion.Data data = (FolderCompletion.Data) o;
            DeviceCard c = getDeviceCard(data.device, devices);
            if (c != null) {
                c.setCompletion(controller.getCompletionTotal(c.getDeviceID()));
            } else {
                if (hasView()) {
                    devices = updateDevices(devices);
                    getView().refreshDevices(devices);
                }
            }
        }

    }

    //TODO only notify on changed device
    void postConnectiosUpdate(MyDeviceCard myDevice, List<DeviceCard> devices) {
        for (DeviceCard c : devices) {
            ConnectionInfo conn = controller.getConnection(c.getDeviceID());
            c.setConnectionInfo(conn);
        }
        ConnectionInfo tConn = controller.getConnectionTotal();
        if (tConn != null) {
            if (myDevice != null) {
                myDevice.setConnectionInfo(tConn);
            } else {
                if (hasView()) {
                    myDevice = updateThisDevice(null);
                    getView().refreshThisDevice(myDevice);
                }
            }
        }
    }

    //TODO only notif on changed device
    void postDeviceStatsUpdate(List<DeviceCard> devices) {
        for (DeviceCard c : devices) {
            DeviceStats s = controller.getDeviceStats(c.getDeviceID());
            if (s != null) {
                c.setDeviceStats(s);
            }
        }
    }

    void onSystemInfoUpdate(MyDeviceCard myDevice) {
        if (myDevice != null) {
            myDevice.setSystemInfo(controller.getSystemInfo());
        } else {
            if (hasView()) {
                myDevice = updateThisDevice(null);
                getView().refreshThisDevice(myDevice);
            }
        }
    }

    void onFolderModelUpdate(Object o, List<FolderCard> folders) {
        if (SessionController.ChangeEvent.NONE == o) {
            if (hasView()) {
                folders = updateFolders(folders);
                getView().refreshFolders(folders);
            }
        } else {
            FolderSummary.Data data = (FolderSummary.Data) o;
            FolderCard fc = getFolderCard(data.folder, folders);
            if (fc != null) {
                fc.setModel(data.summary);
            } else {
                if (hasView()) {
                    folders = updateFolders(folders);
                    getView().refreshFolders(folders);
                }
            }
        }
    }

    void onFolderStateChange(Object o, List<FolderCard> folders) {
        if (SessionController.ChangeEvent.NONE == o) {
            if (hasView()) {
                folders = updateFolders(folders);
                getView().refreshFolders(folders);
            }
        } else {
            StateChanged.Data data = (StateChanged.Data) o;
            FolderCard fc = getFolderCard(data.folder, folders);
            if (fc != null) {
                fc.setState(data.to);
            } else {
                if (hasView()) {
                    folders = updateFolders(folders);
                    getView().refreshFolders(folders);
                }
            }
        }

    }

    public void showSavingDialog() {
        dialogPresenter.showDialog(context -> {
            ProgressDialog mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(context.getResources().getString(R.string.saving_config_dots));
            return mProgressDialog;
        });
    }

    public void dismissSavingDialog() {
        dialogPresenter.dismissDialog();
    }

    void showRestartingDialog() {
        dialogPresenter.showDialog(context -> {
            ProgressDialog mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(context.getResources().getString(R.string.syncthing_is_restarting));
            return mProgressDialog;
        });
    }

    void dismissRestartingDialog() {
        dialogPresenter.dismissDialog();
    }

    public void showError(String title, String msg) {
        dialogPresenter.showDialog(context -> new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .create());
    }

    public void showError(int res, String msg) {
        dialogPresenter.showDialog(context -> new AlertDialog.Builder(context)
                .setTitle(res)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .create());
    }

    public void dismissError() {
        dialogPresenter.dismissDialog();
    }

    public void showSuccessMsg() {
        if (hasView()) {
            Toast.makeText(getView().getContext(), R.string.config_saved, Toast.LENGTH_SHORT).show();
        }
    }

    public String getMyDeviceId() {
        return controller.getMyID();
    }

    public void retryConnection() {
        if (!controller.isRunning()) {
            controller.init();
            if (hasView()) {
                getView().showLoading();
            }
        }
    }

    void overrideChanges(String id) {
        controller.overrideChanges(id, t -> showError(R.string.connection_error, t.getMessage()));
    }

    void scanFolder(String id) {
        controller.scanFolder(id, t -> showError(R.string.connection_error, t.getMessage()));
    }

    void openLoginScreen() {
        activityResultsController.startActivityForResult(
                new Intent(appContext, ManageActivity.class)
                        .putExtra(ManageActivity.EXTRA_CREDENTIALS, (Parcelable) credentials),
                ActivityRequestCodes.LOGIN_ACTIVITY,
                null
        );
    }

    void openAddDeviceScreen() {
        openEditDeviceScreen(null);
    }

    void openEditDeviceScreen(String deviceId) {
        openIntent(getEditIntent(EditDeviceFragment.NAME,
                EditDeviceFragment.makeArgs(credentials, deviceId)));
    }

    void openAddFolderScreen() {
        openEditFolderScreen(null, null);
    }

    void openEditFolderScreen(String folderId) {
        openEditFolderScreen(folderId, null);
    }

    void openEditFolderScreen(String folderId, String deviceId) {
        openIntent(getEditIntent(EditFolderFragment.NAME,
                EditFolderFragment.makeArgs(credentials, folderId, deviceId)));
    }

    public void openEditIgnoresScreen(String folderId) {
        openIntent(getEditIntent(EditIgnoresFragment.NAME,
                EditIgnoresFragment.makeArgs(credentials, folderId)));
    }

    void openSettingsScreen() {
        openIntent(getEditIntent(SettingsFragment.NAME,
                SettingsFragment.makeArgs(credentials)));
    }

    private Intent getEditIntent(String name, Bundle args) {
        Intent i = new Intent(appContext, ManageActivity.class)
                .putExtra(ManageActivity.EXTRA_FRAGMENT, name)
                .putExtra(ManageActivity.EXTRA_ARGS, args);
        return i;
    }

    private void openIntent(Intent i) {
        activityResultsController.startActivityForResult(i, 2, null);
    }

    protected void showIdDialog() {
        if (hasView()) {
            MortarScope myScope = MortarScope.getScope(getView().getContext());
            Context childContext = myScope.createContext(getView().getContext());
            //TODO this *will* leak if not dismissed
            new ShowIdDialog(childContext).show();
        }
    }

    @Override
    public IdenticonGenerator identiconGenerator() {
        return identiconGenerator;
    }

    ActionBarConfig getToolbarConfig() {
        ActionBarConfig.Builder bob = ActionBarConfig.builder()
                .setTitle(credentials.alias)
                ;
        if (controller.isOnline()) {
            bob.setMenuConfig(new SessionMenuHandler(this));
        }
        return bob.build();
    }
}
