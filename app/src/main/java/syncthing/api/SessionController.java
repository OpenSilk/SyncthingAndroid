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

package syncthing.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;
import syncthing.api.model.Completion;
import syncthing.api.model.Config;
import syncthing.api.model.ConfigStats;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.Connections;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.DeviceStats;
import syncthing.api.model.DeviceStatsMap;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import syncthing.api.model.FolderStats;
import syncthing.api.model.FolderStatsMap;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.Ignores;
import syncthing.api.model.Model;
import syncthing.api.model.ModelState;
import syncthing.api.model.OptionsConfig;
import syncthing.api.model.SystemErrors;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.SystemMessage;
import syncthing.api.model.Version;
import syncthing.api.model.event.DevicePaused;
import syncthing.api.model.event.DeviceRejected;
import syncthing.api.model.event.DeviceResumed;
import syncthing.api.model.event.Event;
import syncthing.api.model.event.FolderCompletion;
import syncthing.api.model.event.FolderErrors;
import syncthing.api.model.event.FolderRejected;
import syncthing.api.model.event.FolderScanProgress;
import syncthing.api.model.event.FolderSummary;
import syncthing.api.model.event.StateChanged;
import timber.log.Timber;

/**
 * Created by drew on 3/4/15.
 */
@SessionScope
public class SessionController implements EventMonitor.EventListener {

    public enum Change {
        ONLINE,
        OFFLINE,
        FAILURE,
        COMPLETION, //FolderCompletion.Data or none
        FOLDER_STATS,
        DEVICE_STATS,
        CONNECTIONS_UPDATE,
        CONNECTIONS_CHANGE,
        CONFIG_UPDATE,
        SYSTEM,
        NOTICE,
        //EventMonitor
        NEED_LOGIN,

        DEVICE_DISCOVERED,
        DEVICE_CONNECTED,
        DEVICE_DISCONNECTED,
        DEVICE_PAUSED, //DevicePaused.Data
        DEVICE_REJECTED,
        DEVICE_RESUMED, //DeviceResumed.Data
        LOCAL_INDEX_UPDATED,
        REMOTE_INDEX_UPDATED,
        ITEM_STARTED,
        ITEM_FINISHED, //ItemFinished.Data
        STATE_CHANGED, //StateChanged.Data
        FOLDER_REJECTED,
        CONFIG_SAVED,
        DOWNLOAD_PROGRESS,
        FOLDER_COMPLETION,
        FOLDER_SUMMARY, //FolderSummary.Data
        FOLDER_ERRORS, //FolderErrors.Data
        FOLDER_SCAN_PROGRESS, //FolderScanProgress.Data
    }

    public static class ChangeEvent {
        public static final Object NONE = new Object();
        public final Change change;
        //Event specific data
        public final Object data;

        public ChangeEvent(Change change, Object data) {
            this.change = change;
            this.data = data == null ? NONE : data;
        }

    }

    final AtomicReference<SystemInfo> systemInfo = new AtomicReference<>();
    final AtomicReference<String> myId = new AtomicReference<>();
    final AtomicReference<Config> config = new AtomicReference<>();
    final AtomicBoolean configInSync = new AtomicBoolean();
    //synchronize on self
    final Connections connections = new Connections();
    //synchronize on self
    final DeviceStatsMap deviceStats = new DeviceStatsMap();
    //synchronize on self
    final FolderStatsMap folderStats = new FolderStatsMap();
    final AtomicReference<Version> version = new AtomicReference<>();
    //synchronize on self
    final Map<String, Model> models = new HashMap<>(10);
    //synchronize on self
    final Map<String, FolderConfig> folders = new LinkedHashMap<>(10); //we pre sort this
    //synchronize on self
    final Map<String, DeviceConfig> devices = new LinkedHashMap<>(10); //we pre sort this
    //synchronize on self TODO a map of a map? really???
    final Map<String, Map<String, Float>> completion = new HashMap<>(10);
    //synchronize on self
    final Map<String, DeviceRejected> deviceRejections = new LinkedHashMap<>(); //want displayed in order received
    //synchronize on self
    final Map<String, FolderRejected> folderRejections = new LinkedHashMap<>(); //want displayed in order received
    final AtomicReference<SystemErrors> errorsList = new AtomicReference<>();
    //synchronize on self
    final WeakHashMap<String, Subscription> activeSubscriptions = new WeakHashMap<>();
    //synchronize on self
    final Map<String, FolderScanProgress.Data> folderScanProgress = new HashMap<>();
    //synchronize on self
    final Map<String, List<FolderErrors.Error>> folderErrors = new HashMap<>();

    //Following synchronized by lock
    private final Object lock = new Object();
    boolean online;
    boolean restarting;
    boolean running;

    static final String suspendSubscriptionKey = "suspendSubscription";

    final Scheduler subscribeOn;
    final SyncthingApi restApi;
    final EventMonitor eventMonitor;
    final SerializedSubject<ChangeEvent, ChangeEvent> changeBus =
            BehaviorSubject.<ChangeEvent>create().toSerialized();

    @Inject
    public SessionController(SyncthingApi restApi, @Named("longpoll") SyncthingApi longpollRestApi) {
        Timber.i("new SessionController");
        this.subscribeOn = Schedulers.io();//TODO allow configure
        this.restApi = SynchingApiWrapper.wrap(restApi, subscribeOn);
        this.eventMonitor = new EventMonitor(longpollRestApi, this);
    }

    public void init() {
        synchronized (lock) {
            Subscription s = removeSubscription(suspendSubscriptionKey);
            if (s != null) {
                s.unsubscribe();
            }
            if (!eventMonitor.isRunning()) {
                eventMonitor.start();
            }
            running = true;
        }
    }

    public void suspend() {
        synchronized (lock) {
            if (running) {
                Subscription s = removeSubscription(suspendSubscriptionKey);
                if (s != null) {
                    s.unsubscribe();
                }
                // add delay to allow for configuration changes
                s = Observable.timer(30, TimeUnit.SECONDS, subscribeOn)
                        .subscribe(ii -> {
                            eventMonitor.stop();
                            removeSubscription(suspendSubscriptionKey);
                        });
                addSubscription(suspendSubscriptionKey, s);
                running = false;
            }
        }
    }

    /*package*/ void kill() {
        final Scheduler.Worker worker = subscribeOn.createWorker();
        worker.schedule(() -> {
            synchronized (lock) {
                eventMonitor.stop();
                running = false;
            }
            unsubscribeActiveSubscriptions();
            worker.unsubscribe();
        });
    }

    public boolean isRunning() {
        synchronized (lock) {
            return running;
        }
    }

    public void handleEvent(Event e) {
        if (updateState(true)) {
            Timber.d("Eating event %s", e.type);
        }
        Timber.d("New event %s", e.type);
        switch (e.type) {
            case STARTUP_COMPLETE: {
                break;
            } case STATE_CHANGED: {
                StateChanged st = (StateChanged) e;
                boolean incremental = false;
                synchronized (models) {
                    if (models.containsKey(st.data.folder)) {
                        models.get(st.data.folder).state = st.data.to;
                        incremental = true;
                    }
                }
                if (st.data.to == ModelState.SCANNING) {
                    //remove any stale scan progress
                    synchronized (folderScanProgress) {
                        folderScanProgress.remove(st.data.folder);
                    }
                }
                if (incremental) {
                    postChange(Change.STATE_CHANGED, st.data);
                } else {
                    refreshFolder(st.data.folder);
                }
                break;
            } case LOCAL_INDEX_UPDATED: {
                onLocalIndexUpdated(e);
                break;
            } case REMOTE_INDEX_UPDATED: {
                //pass
                break;
            } case DEVICE_CONNECTED: {
                refreshConnections();
                refreshDeviceStats();
                break;
            } case DEVICE_DISCONNECTED: {
                refreshConnections();
                refreshDeviceStats();
                break;
            } case DEVICE_DISCOVERED: {
                break;
            } case DEVICE_PAUSED: {
                String id = ((DevicePaused.Data)e.data).device;
                synchronized (connections) {
                    if (connections.connections.containsKey(id)) {
                        connections.connections.get(id).paused = true;
                    }
                }
                postChange(Change.DEVICE_PAUSED, e.data);
                break;
            } case DEVICE_RESUMED: {
                String id = ((DeviceResumed.Data)e.data).device;
                synchronized (connections) {
                    if (connections.connections.containsKey(id)) {
                        connections.connections.get(id).paused = false;
                    }
                }
                postChange(Change.DEVICE_RESUMED, e.data);
                break;
            } case DEVICE_REJECTED: {
                DeviceRejected dr = (DeviceRejected) e;
                if (getDevice(dr.data.device) == null) {
                    synchronized (deviceRejections) {
                        deviceRejections.put(dr.data.device, dr);
                    }
                    postChange(Change.DEVICE_REJECTED);
                } else {
                    Timber.w("Ignoring DEVICE_REJECTED for %s", dr.data.device);
                }
                break;
            } case FOLDER_REJECTED: {
                FolderRejected fr = (FolderRejected) e;
                if (getFolder(fr.data.folder) == null) {
                    synchronized (folderRejections) {
                        folderRejections.put(fr.data.folder + "★" + fr.data.device, fr);
                    }
                    postChange(Change.FOLDER_REJECTED);
                } else {
                    Timber.w("Ignoring FOLDER_REJECTED for %s from %s", fr.data.folder, fr.data.device);
                }
                break;
            } case CONFIG_SAVED: {
                refreshConfig();
                break;
            } case DOWNLOAD_PROGRESS: {
                break;
            } case FOLDER_SUMMARY: {
                FolderSummary fs = (FolderSummary) e;
                updateModel(fs.data.folder, fs.data.summary);
                postChange(Change.FOLDER_SUMMARY, fs.data);
                break;
            } case FOLDER_COMPLETION: {
                FolderCompletion fc = (FolderCompletion) e;
                updateCompletion(fc.data.device, fc.data.folder, new Completion(fc.data.completion));
                postChange(Change.COMPLETION, fc.data);
                break;
            } case FOLDER_ERRORS: {
                FolderErrors fe = (FolderErrors) e;
                updateFolderErrors(fe.data.folder, fe.data.errors);
                postChange(Change.FOLDER_ERRORS, fe.data);
                break;
            } case FOLDER_SCAN_PROGRESS: {
                FolderScanProgress.Data d = (FolderScanProgress.Data) e.data;
                synchronized (folderScanProgress) {
                    folderScanProgress.put(d.folder, d);
                }
                postChange(Change.FOLDER_SCAN_PROGRESS, d);
                break;
            } case ITEM_FINISHED: {
                postChange(Change.ITEM_FINISHED, e.data);
                break;
            } case ITEM_STARTED: {
                break;
            } case RELAY_STATE_CHANGED: {
                refreshSystem();
                break;
            } case PING: {
                refreshSystem();
                refreshConnections(true);
                refreshErrors();
                break;
            } default: {
                break;
            }
        }
    }

    @Override
    public void onError(EventMonitor.Error e) {
        Timber.w("onError %s", e.toString());
        switch (e) {
            case UNAUTHORIZED:
                updateState(false);
                postChange(Change.NEED_LOGIN);
                break;
            case DISCONNECTED:
                updateState(false);
                break;
            case STOPPING:
                synchronized (lock) {
                    running = false;
                }
                updateState(false);
                postChange(Change.FAILURE);
                break;
        }
    }

    static final String updateStateKey = "updateState";
    boolean updateState(boolean online) {
        synchronized (lock) {
            //No state change dont eat event;
            if (this.online == online) return false;
            //New event came in while we are initializing, eat it
            if (online && hasActiveSubscription(updateStateKey)) return true;
            if (online) {
                this.restarting = false;
                //Our online state depends on all these items
                //so we merge them together so we can defer
                //posting the ONLINE status until we have set all the values
                Subscription s = Observable.merge(
                        retryOnce(restApi.system()).map(r -> Pair.of(1, r)),
                        retryOnce(restApi.config()).map(r -> Pair.of(2, r)),
                        retryOnce(restApi.configStatus()).map(r -> Pair.of(3, r)),
                        retryOnce(restApi.connections()).map(r -> Pair.of(4, r)),
                        retryOnce(restApi.deviceStats()).map(r -> Pair.of(5, r)),
                        retryOnce(restApi.folderStats()).map(r -> Pair.of(6, r)),
                        retryOnce(restApi.version()).map(r -> Pair.of(7, r))
                ).subscribe(
                        (p) -> {
                            switch (p.getLeft()) {
                                case 1:
                                    updateSystemInfo((SystemInfo) p.getRight());
                                    break;
                                case 2:
                                    updateConfig((Config) p.getRight());
                                    break;
                                case 3:
                                    updateConfigStats((ConfigStats) p.getRight());
                                    break;
                                case 4:
                                    updateConnections((Connections) p.getRight());
                                    break;
                                case 5:
                                    setDeviceStats((DeviceStatsMap) p.getRight());
                                    break;
                                case 6:
                                    setFolderStats((FolderStatsMap) p.getRight());
                                    break;
                                case 7:
                                    setVersion((Version) p.getRight());
                                    break;
                            }
                        },
                        (t) -> {
                            synchronized (lock) {
                                this.online = false;
                                logException(t, updateStateKey);
                            }
                            //TODO this isnt really the right thing to do
                            postChange(Change.FAILURE);
                        },
                        () -> {
                            synchronized (lock) {
                                this.online = true;
                                removeSubscription(updateStateKey);
                            }
                            postChange(Change.ONLINE);
                        }
                );
                addSubscription(updateStateKey, s);
            } else {
                this.online = false;
                removeSubscription(updateStateKey);
                postChange(Change.OFFLINE);
            }
            return true;
        }
    }

    void postChange(Change change) {
        postChange(change, null);
    }

    void postChange(Change change, Object data) {
        switch (change) {
            case OFFLINE:
            case NEED_LOGIN:
            case FAILURE:
                sendChangeEvent(new ChangeEvent(change, data));
                return;
            default:
                if (isOnline()) {
                    sendChangeEvent(new ChangeEvent(change, data));
                } else {
                    Timber.w("Dropping change %s while offline", change.toString());
                }
        }
    }

    void sendChangeEvent(ChangeEvent event) {
        changeBus.onNext(event);
    }

    static final String refreshSystemKey = "refreshSystem";
    public void refreshSystem() {
        if (hasActiveSubscription(refreshSystemKey)) return;
        Subscription s = restApi.system()
                .subscribe(
                        this::updateSystemInfo,
                        (t) -> logException(t, refreshSystemKey),
                        () -> {
                            postChange(Change.SYSTEM);
                            removeSubscription(refreshSystemKey);
                        }
                );
        addSubscription(refreshSystemKey, s);
    }

    static final String refreshConfigKey = "refreshConfig";
    public void refreshConfig() {
        if (hasActiveSubscription(refreshConfigKey)) return;
        Subscription s = Observable.merge(
                restApi.config().map(r -> Pair.of(1, r)),
                restApi.configStatus().map(r -> Pair.of(2, r))
        ).subscribe(
                p -> {
                    switch (p.getLeft()) {
                        case 1:
                            updateConfig((Config) p.getRight());
                            break;
                        case 2:
                            updateConfigStats((ConfigStats) p.getRight());
                            break;
                    }
                },
                (t) -> logException(t, refreshConfigKey),
                () -> {
                    postChange(Change.CONFIG_UPDATE);
                    removeSubscription(refreshConfigKey);
                }
        );
        addSubscription(refreshConfigKey, s);
    }

    public void refreshConnections() {
        refreshConnections(false);
    }

    static final String refreshConnectionsKey = "refreshConnections";
    public void refreshConnections(boolean update) {
        if (hasActiveSubscription(refreshConnectionsKey)) return;
        Subscription s = restApi.connections()
                .subscribe(
                        this::updateConnections,
                        (t) -> logException(t, refreshConnectionsKey),
                        () -> {
                            postChange(update ? Change.CONNECTIONS_UPDATE : Change.CONNECTIONS_CHANGE);
                            removeSubscription(refreshConnectionsKey);
                        });
        addSubscription(refreshConnectionsKey, s);
    }

    static final String refreshDeviceStatsKey = "refreshDeviceStats";
    public void refreshDeviceStats() {
        if (hasActiveSubscription(refreshDeviceStatsKey)) return;
        Subscription s = restApi.deviceStats()
                .subscribe(
                        this::setDeviceStats,
                        (t) -> logException(t, refreshDeviceStatsKey),
                        () -> {
                            postChange(Change.DEVICE_STATS);
                            removeSubscription(refreshDeviceStatsKey);
                        }
                );
        addSubscription(refreshDeviceStatsKey, s);
    }

    static final String refreshFolderStatsKey = "refreshFolderStats";
    public void refreshFolderStats() {
        if (hasActiveSubscription(refreshFolderStatsKey)) return;
        Subscription s = restApi.folderStats()
                .subscribe(
                        this::setFolderStats,
                        (t) -> logException(t, refreshFolderStatsKey),
                        () -> {
                            postChange(Change.FOLDER_STATS);
                            removeSubscription(refreshFolderStatsKey);
                        }
                );
        addSubscription(refreshFolderStatsKey, s);
    }

    public void refreshVersion() {
        Subscription s = restApi.version()
                .subscribe(this::setVersion, this::logException);
    }

    public void refreshFolder(String name) {
        final String key = "refreshFolder+"+name;
        if (hasActiveSubscription(key)) return;
        Subscription s = restApi.model(name)
                .subscribe(
                        model -> {
                            updateModel(name, model);
                            postChange(Change.FOLDER_SUMMARY, new FolderSummary.Data(name, model));
                        },
                        (t) -> logException(t, key),
                        () -> removeSubscription(key)
                );
        addSubscription(key, s);
    }

    public void refreshFolders(Collection<String> names) {
        for (String name : names) {
            refreshFolder(name);
        }
    }

    public void refreshCompletion(String device, String folder) {
        Subscription s = restApi.completion(device, folder)
                .subscribe(
                        (comp) -> {
                            updateCompletion(device, folder, comp);
                        },
                        this::logException,
                        () -> postChange(Change.COMPLETION)
                );
    }

    //Wants Device,Folder pair
    public void refreshCompletions(Collection<Pair<String, String>> refreshers) {
        if (refreshers.isEmpty()) {
            return;
        }
        //Same as refreshFolders but we need to keep track of folders and devices
        List<Observable<Triple<String, String, Completion>>> observables = new ArrayList<>();
        for (Pair<String, String> refresh : refreshers) {
            observables.add(Observable.zip(
                    Observable.just(refresh.getLeft()),//device
                    Observable.just(refresh.getRight()),//folder
                    restApi.completion(refresh.getLeft(), refresh.getRight()).first(),
                    Triple::of)
            );
        }
        Subscription s = Observable.merge(observables)
                .subscribe(
                        (triple) -> updateCompletion(triple.getLeft(), triple.getMiddle(), triple.getRight()),
                        this::logException,
                        () -> postChange(Change.COMPLETION)
                );
    }


    static final String localIndexUpdatedKey = "localIndexUpdated";
    void onLocalIndexUpdated(Event e) {
        if (hasActiveSubscription(localIndexUpdatedKey)) return;
        Subscription s = Observable.timer(500, TimeUnit.MILLISECONDS, subscribeOn)
                .subscribe(
                        ii -> refreshFolderStats(),
                        (t) -> logException(t, localIndexUpdatedKey),
                        () -> removeSubscription(localIndexUpdatedKey)
                );
        addSubscription(localIndexUpdatedKey, s);
    }

    public boolean isOnline() {
        synchronized (lock) {
            return online;
        }
    }

    public boolean isRestarting() {
        synchronized (lock) {
            return restarting;
        }
    }

    public SystemInfo getSystemInfo() {
        return systemInfo.get();
    }

    public String getMyID() {
        return myId.get();
    }

    void updateSystemInfo(SystemInfo systemInfo) {
        this.myId.set(systemInfo.myID);
        this.systemInfo.set(systemInfo);
    }

    public Config getConfig() {
        return config.get();
    }

    void updateConfig(Config config) {
        Collections.sort(config.folders, (lhs, rhs) -> lhs.id.compareTo(rhs.id));
        synchronized (folders) {
            folders.clear();
            for (FolderConfig f : config.folders) {
                folders.put(f.id, f);
            }
        }
        Collections.sort(config.devices, (lhs, rhs) -> lhs.deviceID.compareTo(rhs.deviceID));
        synchronized (devices) {
            devices.clear();
            for (DeviceConfig d : config.devices) {
                devices.put(d.deviceID, d);
            }
        }
        int fold, fnew;
        synchronized (folderRejections) {
            fold = folderRejections.size();
            //remove any stale rejections
            Iterator<Map.Entry<String, FolderRejected>> ii = folderRejections.entrySet().iterator();
            while (ii.hasNext()) {
                String[] split = StringUtils.split(ii.next().getKey(), '★');
                if (split != null && split.length > 0) {
                    if (getFolder(split[0]) != null) {
                        ii.remove();
                    }
                }
            }
            fnew = folderRejections.size();
        }
        int dold, dnew;
        synchronized (deviceRejections) {
            dold = deviceRejections.size();
            //remove any stale rejections
            Iterator<Map.Entry<String, DeviceRejected>> ii = deviceRejections.entrySet().iterator();
            while (ii.hasNext()) {
                if (getDevice(ii.next().getKey()) != null) {
                    ii.remove();
                }
            }
            dnew = deviceRejections.size();
        }
        if (fold != fnew || dold != dnew) {
            postChange(Change.NOTICE);
        }
        this.config.set(config);
    }

    public boolean isConfigInSync() {
        return configInSync.get();
    }

    void updateConfigStats(ConfigStats configStats) {
        this.configInSync.set(configStats.configInSync);
    }

    public @Nullable ConnectionInfo getConnection(String id) {
        synchronized (connections) {
            return connections.connections.get(id);
        }
    }

    public ConnectionInfo getConnectionTotal() {
        synchronized (connections) {
            return connections.total;
        }
    }

    void updateConnections(Connections conns) {
        long now = System.currentTimeMillis();
        synchronized (connections) {
            for (String key : conns.connections.keySet()) {
                ConnectionInfo newC = conns.connections.get(key);
                newC.deviceId = key;
                newC.lastUpdate = now;
                if (connections.connections.containsKey(key)) {
                    ConnectionInfo oldC = connections.connections.get(key);
                    long td = (now - oldC.lastUpdate) / 1000;
                    if (td > 0) {
                        newC.inbps = Math.max(0, (newC.inBytesTotal - oldC.inBytesTotal) / td);
                        newC.outbps = Math.max(0, (newC.outBytesTotal - oldC.outBytesTotal) / td);
                    }
                }
                //also update completion
                resetCompletionTotal(key);
            }
            connections.connections = conns.connections;
            connections.total = conns.total;
        }
    }

    public DeviceStats getDeviceStats(String id) {
        synchronized (deviceStats) {
            return deviceStats.get(id);
        }
    }

    void setDeviceStats(DeviceStatsMap deviceStats) {
        synchronized (this.deviceStats) {
            this.deviceStats.clear();
            this.deviceStats.putAll(deviceStats);
        }
    }

    public FolderStats getFolderStats(String name) {
        synchronized (folderStats) {
            return folderStats.get(name);
        }
    }

    void setFolderStats(FolderStatsMap folderStats) {
        synchronized (this.folderStats) {
            this.folderStats.clear();
            this.folderStats.putAll(folderStats);
        }
    }

    public Version getVersion() {
        return version.get();
    }

    void setVersion(Version version) {
        this.version.set(version);
    }

    public @Nullable Model getModel(String folderName) {
        synchronized (models) {
            return models.get(folderName);
        }
    }

    void updateModel(String folderName, Model model) {
        Timber.d("updateModel(%s) m=%s", folderName, model);
        synchronized (models) {
            if (models.containsKey(folderName)) {
                models.remove(folderName);
            }
            models.put(folderName, model);
        }
    }

    public @Nullable FolderConfig getFolder(String name) {
        synchronized (folders) {
            return folders.get(name);
        }
    }

    public List<FolderConfig> getFolders() {
        synchronized (folders) {
            return new ArrayList<>(folders.values());
        }
    }

    public @Nullable FolderScanProgress.Data getFolderScanProgress(String folder) {
        synchronized (folderScanProgress) {
            return folderScanProgress.get(folder);
        }
    }

    public List<DeviceConfig> getDevices() {
        synchronized (devices) {
            return new ArrayList<>(devices.values());
        }
    }

    public @Nullable DeviceConfig getThisDevice() {
        synchronized (devices) {
            return devices.get(getMyID());
        }
    }

    public @NonNull List<DeviceConfig> getRemoteDevices() {
        Map<String, DeviceConfig> devs;
        synchronized (devices) {
            devs = new HashMap<>(devices);
        }
        devs.remove(getMyID());
        return new ArrayList<>(devs.values());
    }

    public @Nullable DeviceConfig getDevice(String deviceId) {
        synchronized (devices) {
            return devices.get(deviceId);
        }
    }

    void updateCompletion(String device, String folder, Completion comp) {
        Timber.d("Updating completion for %s", device);
        synchronized (completion) {
            if (!completion.containsKey(device)) {
                completion.put(device, new HashMap<String, Float>());
            }
            completion.get(device).put(folder, comp.completion);
            float tot = 0;
            int cnt = 0;
            for (String key : completion.get(device).keySet()) {
                if ("_total".equals(key)) continue;
                tot += completion.get(device).get(key);
                cnt++;
            }
            completion.get(device).put("_total", tot / cnt);
        }
    }

    void resetCompletionTotal(String deviceId) {
        synchronized (completion) {
            if (!completion.containsKey(deviceId)) {
                completion.put(deviceId, new HashMap<String, Float>());
            }
            completion.get(deviceId).put("_total", 100f);
        }
    }

    public int getCompletionTotal(String deviceId) {
        synchronized (completion) {
            if (completion.containsKey(deviceId)) {
                return Math.min(100, Math.round(completion.get(deviceId).get("_total")));
            } else {
                return -1;
            }
        }
    }

    public @Nullable Map<String, Float> getCompletionStats(String deviceId) {
        synchronized (completion) {
            return Collections.unmodifiableMap(completion.get(deviceId));
        }
    }

    public Set<Map.Entry<String, DeviceRejected>> getDeviceRejections() {
        synchronized (deviceRejections) {
            return Collections.unmodifiableSet(deviceRejections.entrySet());
        }
    }

    public void removeDeviceRejection(String key) {
        synchronized (deviceRejections) {
            deviceRejections.remove(key);
        }
        postChange(Change.DEVICE_REJECTED);
    }

    public Set<Map.Entry<String, FolderRejected>> getFolderRejections() {
        synchronized (folderRejections) {
            return Collections.unmodifiableSet(folderRejections.entrySet());
        }
    }

    public void removeFolderRejection(String key) {
        synchronized (folderRejections) {
            folderRejections.remove(key);
        }
        postChange(Change.FOLDER_REJECTED);
    }

    static final String refreshErrorsKey = "refreshErrors";
    public void refreshErrors() {
        if (hasActiveSubscription(refreshErrorsKey)) return;
        Subscription s = restApi.errors()
                .subscribe(
                        errorsList::set,
                        (t) -> logException(t, refreshErrorsKey),
                        () -> {
                            postChange(Change.NOTICE);
                            removeSubscription(refreshErrorsKey);
                        }
                );
        addSubscription(refreshErrorsKey, s);
    }

    static final String clearErrorsKey = "clearErrors";
    public void clearErrors() {
        if (hasActiveSubscription(clearErrorsKey)) return;
        Subscription s = restApi.clearErrors().subscribe(
                ignoreOnNext(),
                (t) -> logException(t, clearErrorsKey),
                () -> {
                    errorsList.set(null);
                    removeSubscription(clearErrorsKey);
                    postChange(Change.NOTICE);
                });
        addSubscription(clearErrorsKey, s);
    }

    public @Nullable SystemMessage getLatestError() {
        List<SystemMessage> errors = errorsList.get() != null ? errorsList.get().errors : null;
        if (errors != null && errors.size() > 0) {
            return errors.get(errors.size() - 1);
        } else {
            return null;
        }
    }

    void updateFolderErrors(String folder, List<FolderErrors.Error> errors) {
        synchronized (folderErrors) {
            folderErrors.put(folder, errors);
        }
    }

    public @NonNull List<FolderErrors.Error> getFolderErrors(String folder) {
        synchronized (folderErrors) {
            List<FolderErrors.Error> errors = folderErrors.get(folder);
            if (errors != null) {
                return new ArrayList<>(errors);
            } else {
                return Collections.emptyList();
            }
        }
    }

    public Subscription editFolder(FolderConfig folder, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.config()
                .map((config) -> {
                    if (config.folders.isEmpty()) {
                        config.folders = Collections.singletonList(folder);
                    } else {
                        int index = config.folders.indexOf(folder);
                        if (index < 0) {
                            config.folders.add(folder);
                        } else {
                            config.folders.set(index, folder);
                        }
                    }
                    return config;
                })
                .flatMap(restApi::updateConfig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignoreOnNext(),
                        onError,
                        onComplete
                );
    }

    public Subscription deleteFolder(FolderConfig folder, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.config()
                .map((config) -> {
                    if (!config.folders.remove(folder)) {
                        throw new IllegalArgumentException("Folder " + folder.id + " not found in config");
                    }
                    config.folders.remove(folder);
                    return config;
                })
                .flatMap(restApi::updateConfig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignoreOnNext(),
                        onError,
                        onComplete
                );
    }

    public Subscription shareFolder(String name, String devId, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.config().zipWith(restApi.deviceId(devId),
                (config, deviceId) -> {
                    if (deviceId.id == null) {
                        throw new NullPointerException(deviceId.error);
                    }
                    boolean folderUpdated = false;
                    for (FolderConfig folder : config.folders) {
                        if (StringUtils.equals(folder.id, name)) {
                            if (folder.devices.isEmpty()) {
                                folder.devices = new ArrayList<>();
                            }
                            for (FolderDeviceConfig d : folder.devices) {
                                if (StringUtils.equals(d.deviceID, deviceId.id)) {
                                    throw new IllegalArgumentException("Folder already shared with device "
                                            + d.deviceID);
                                }
                            }
                            folder.devices.add(new FolderDeviceConfig(deviceId.id));
                            folderUpdated = true;
                            break;
                        }
                    }
                    if (!folderUpdated) {
                        throw new IllegalArgumentException("Folder doesn't exist " + name);
                    }
                    return config;
                })
                .flatMap(restApi::updateConfig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignoreOnNext(),
                        onError,
                        onComplete
                );

    }

    public Subscription editDevice(DeviceConfig device, Map<String, Boolean> folders, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.deviceId(device.deviceID)
                // fetched the normalized id
                // then we update the device with the new id
                // and update the config
                .zipWith(restApi.config(), (deviceId, config) -> {
                    if (deviceId.id == null) {
                        throw new NullPointerException(deviceId.error);
                    }
                    Timber.d("editDevice() updating deviceId %s -> %s", device.deviceID, deviceId.id);
                    device.deviceID = deviceId.id;

                    if (config.devices.isEmpty()) {
                        config.devices = Collections.singletonList(device);
                    } else {
                        int index = config.devices.indexOf(device);
                        if (index < 0) {
                            config.devices.add(device);
                        } else {
                            config.devices.set(index, device);
                        }
                    }
                    if (folders != null && !folders.isEmpty()) {
                        for (FolderConfig f : config.folders) {
                            if (folders.containsKey(f.id)) {
                                boolean wants = folders.get(f.id);
                                if (f.devices == null || f.devices.isEmpty()) {
                                    f.devices = new ArrayList<>();
                                }
                                boolean found = false;
                                for (FolderDeviceConfig d : f.devices) {
                                    if (StringUtils.equals(d.deviceID, device.deviceID)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (found && !wants) {
                                    f.devices.remove(new FolderDeviceConfig(device.deviceID));
                                } else if (!found && wants) {
                                    f.devices.add(new FolderDeviceConfig(device.deviceID));
                                }
                            }
                        }
                    }
                    return config;
                })
                        // send our edited config back to the server
                .flatMap(restApi::updateConfig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignoreOnNext(),
                        onError,
                        onComplete
                );
    }

    public Subscription deleteDevice(DeviceConfig device, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.deviceId(device.deviceID)
                // got normalized device id
                // need to get config and remove device with matching
                .zipWith(restApi.config(), (deviceId, config) -> {
                    if (deviceId.id == null) {
                        throw new NullPointerException(deviceId.error);
                    }
                    Timber.d("deleteDevice() updating deviceId %s -> %s", device.deviceID, deviceId.id);
                    device.deviceID = deviceId.id;

                    if (!config.devices.remove(device)) {
                        throw new IllegalArgumentException("Device not found in config " + device.deviceID);
                    }
                    return config;
                })
                        // send our altered config back to server
                .flatMap(restApi::updateConfig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignoreOnNext(),
                        onError,
                        onComplete
                );
    }

    public Subscription ignoreDevice(String id, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.deviceId(id)
                .zipWith(restApi.config(), (deviceId, config) -> {
                    if (deviceId.id == null) {
                        throw new NullPointerException(deviceId.error);
                    }
                    if (!config.ignoredDevices.contains(deviceId.id)) {
                        config.ignoredDevices.add(deviceId.id);
                    }
                    return config;
                })
                .flatMap(restApi::updateConfig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignoreOnNext(),
                        onError,
                        onComplete
                );
    }

    public Subscription editSettings(DeviceConfig thisDevice, OptionsConfig options,
                                     GUIConfig guiConfig, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.config()
                .map(config -> {
                    int idx = config.devices.indexOf(thisDevice);
                    config.devices.set(idx, thisDevice);
                    config.options = options;
                    config.gui = guiConfig;
                    return config;
                })
                .flatMap(restApi::updateConfig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignoreOnNext(),
                        onError,
                        onComplete
                );
    }

    public Subscription getIgnores(String id, Action1<Ignores> onNext, Action1<Throwable> onError) {
        return restApi.ignores(id)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, onError);
    }

    public Subscription editIgnores(String id, Ignores ignores, Action1<Ignores> onNext, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.updateIgnores(id, ignores)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, onError, onComplete);
    }

    public void restart() {
        final Scheduler.Worker worker = subscribeOn.createWorker();
        worker.schedule(() -> {
            synchronized (lock) {
                restarting = true;
                eventMonitor.resetCounter();
                updateState(false);
            }
            unsubscribeActiveSubscriptions();
            //synchronous call
            SynchingApiWrapper.unwrap(restApi)
                    .restart().subscribe(ignoreOnNext(), SessionController.this::logException);
            worker.unsubscribe();
        });
    }

    public void shutdown() {
        //push to worker thread so we can stop event monitor
        final Scheduler.Worker worker = subscribeOn.createWorker();
        worker.schedule(() -> {
            synchronized (lock) {
                restarting = false;
                eventMonitor.stop();
                updateState(false);
                postChange(Change.FAILURE);
            }
            unsubscribeActiveSubscriptions();
            //synchronous call
            SynchingApiWrapper.unwrap(restApi)
                    .shutdown().subscribe(ignoreOnNext(), SessionController.this::logException);
            worker.unsubscribe();
        });
    }

    public Observable<List<String>> getAutoCompleteDirectoryList(String current) {
        //intentionally not setting observeOn
        return restApi.autocompleteDirectory(current);
    }

    public Observable<Bitmap> getQRImage(String deviceId) {
        //intentionally not setting observeOn
        return restApi.qr(deviceId)
                .map(resp -> {
                    InputStream in = null;
                    try {
                        in = resp.byteStream();
                        return BitmapFactory.decodeStream(in);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        IOUtils.closeQuietly(in);
                        IOUtils.closeQuietly(resp);
                    }
                });
    }

    public Subscription overrideChanges(String id) {
        return restApi.override(id)
                .subscribe(ignoreOnNext(), this::logException);
    }

    public Subscription scanFolder(String id) {
        return restApi.scan(id)
                .subscribe(ignoreOnNext(), this::logException);
    }


    public Subscription pauseDevice(String deviceId) {
        return restApi.pause(deviceId)
                .subscribe(ignoreOnNext(), this::logException);
    }

    public Subscription resumeDevice(String deviceId) {
        return restApi.resume(deviceId)
                .subscribe(ignoreOnNext(), this::logException);
    }

    void logException(Throwable e) {
        Timber.e(e, "%s: %s", e.getClass().getSimpleName(), e.getMessage());
    }

    void logException(Throwable e, String key) {
        Timber.e(e, "%s: %s", key, e.getMessage());
        removeSubscription(key);
    }

    void addSubscription(String key, Subscription subscription) {
        synchronized (activeSubscriptions) {
            activeSubscriptions.put(key, subscription);
        }
    }

    @Nullable Subscription removeSubscription(String key) {
        synchronized (activeSubscriptions) {
            return activeSubscriptions.remove(key);
        }
    }

    boolean hasActiveSubscription(String key) {
        synchronized (activeSubscriptions) {
            return activeSubscriptions.containsKey(key);
        }
    }

    void unsubscribeActiveSubscriptions() {
        synchronized (activeSubscriptions) {
            for (Subscription s : activeSubscriptions.values()) {
                if (s != null) s.unsubscribe();
            }
            activeSubscriptions.clear();
        }
    }

    private static <T> Observable<T> retryOnce(Observable<T> o) {
        return Observable.defer(() -> o).retry(1);
    }

    private static <T> Action1<T> ignoreOnNext() {
        return t -> {};
    }

    public Subscription subscribeChanges(Action1<ChangeEvent> onNext, Change... changes) {
        Observable<ChangeEvent> o;
        if (changes.length == 0) {
            o = changeBus;
        } else {
            o = changeBus
                    .filter(c -> {
                        for (Change cc : changes) {
                            if (c.change == cc) return true;
                        }
                        return false;
                    });
        }
        final boolean online;
        synchronized (lock) {
            online = this.online;
        }
        return o.onBackpressureBuffer()
                //always post online event for new subscribers
                .startWith(new ChangeEvent(online ? Change.ONLINE : Change.OFFLINE, null))
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onNext);
    }

}
