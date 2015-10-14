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
import android.os.Looper;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import syncthing.api.model.Completion;
import syncthing.api.model.Config;
import syncthing.api.model.ConfigStats;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.Connections;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.DeviceStats;
import syncthing.api.model.DeviceStatsMap;
import syncthing.api.model.event.DeviceRejected;
import syncthing.api.model.event.Event;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import syncthing.api.model.FolderStats;
import syncthing.api.model.FolderStatsMap;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.GuiError;
import syncthing.api.model.GuiErrors;
import syncthing.api.model.Ignores;
import syncthing.api.model.Model;
import syncthing.api.model.OptionsConfig;
import syncthing.api.model.Report;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;
import syncthing.api.model.event.FolderCompletion;
import syncthing.api.model.event.FolderRejected;
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
        MODEL,
        MODEL_STATE,
        COMPLETION,
        FOLDER_STATS,
        DEVICE_STATS,
        CONNECTIONS_UPDATE,
        CONNECTIONS_CHANGE,
        DEVICE_REJECTED,
        FOLDER_REJECTED,
        CONFIG_UPDATE,
        SYSTEM,
        NOTICE,
        //EventMonitor
        NEED_LOGIN,

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
    final AtomicReference<Report> report = new AtomicReference<>();
    //synchronize on self
    final Map<String, Model> models = new LinkedHashMap<>(10);
    //synchronize on self
    final Map<String, FolderConfig> folders = new LinkedHashMap<>(10);
    //synchronize on self
    final List<DeviceConfig> devices = new LinkedList<>();
    //synchronize on self TODO a map of a map? really???
    final Map<String, Map<String, Integer>> completion = new LinkedHashMap<>(10);
    //synchronize on self
    final Map<String, DeviceRejected> deviceRejections = new LinkedHashMap<>();
    //synchronize on self
    final Map<String, FolderRejected> folderRejections = new LinkedHashMap<>();
    //synchronize on self
    final AtomicReference<GuiErrors> errorsList = new AtomicReference<>();

    //Following synchronized by lock
    private final Object lock = new Object();
    long prevDate;
    boolean online;
    boolean restarting;
    boolean running;
    Subscription onlineSub;
    Subscription subspendSubscription;
    Subscription periodicRefreshSubscription;
    Subscription onLocalIndexUpdatedSubscription;

    final Scheduler subscribeOn;
    final SyncthingApi restApi;
    final EventMonitor eventMonitor;
    final BehaviorSubject<ChangeEvent> changeBus = BehaviorSubject.create();

    @Inject
    public SessionController(SyncthingApi restApi, @Named("longpoll") SyncthingApi longpollRestApi) {
        Timber.i("new SessionController");
        this.subscribeOn = Schedulers.io();//TODO allow configure
        this.restApi = SynchingApiWrapper.wrap(restApi, subscribeOn);
        this.eventMonitor = new EventMonitor(longpollRestApi, this);
    }

    public void init() {
        synchronized (lock) {
            if (subspendSubscription != null
                    && !subspendSubscription.isUnsubscribed()) {
                subspendSubscription.unsubscribe();
            } else if (!eventMonitor.isRunning()) {
                eventMonitor.start();
            }
            if (online) {
                setupPeriodicRefresh();
            }
            running = true;
        }
    }

    public void suspend() {
        synchronized (lock) {
            if (running) {
                if (subspendSubscription != null) {
                    subspendSubscription.unsubscribe();
                }
                // add delay to allow for configuration changes
                subspendSubscription = Observable.timer(30, TimeUnit.SECONDS, subscribeOn)
                        .subscribe(ii -> {
                            if (eventMonitor.isRunning()) {
                                eventMonitor.stop();
                            }
                        });
                cancelPeriodicRefresh();
                running = false;
            }
        }
    }

    //Called by main thread, must push to background to stop event monitor
    /*package*/ void kill() {
        final Scheduler.Worker worker = subscribeOn.createWorker();
        worker.schedule(new Action0() {
            @Override
            public void call() {
                synchronized (lock) {
                    if (running) {
                        if (subspendSubscription != null) {
                            subspendSubscription.unsubscribe();
                        }
                        eventMonitor.stop();
                        cancelPeriodicRefresh();
                        running = false;
                    }
                }
                worker.unsubscribe();
            }
        });
    }

    public boolean isRunning() {
        synchronized (lock) {
            return running;
        }
    }

    public void handleEvent(Event e) {
        if (updateState(true)) {
            switch (e.type) {
                case DEVICE_REJECTED:
                case FOLDER_REJECTED:
                    break; //Don't know of another way to get these so pass them through
                default:
                    Timber.d("Eating event %s", e.type);
                    return;//Eat event
            }
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
                if (incremental) {
                    postChange(Change.MODEL_STATE, st.data);
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
            } case DEVICE_REJECTED: {
                DeviceRejected dr = (DeviceRejected) e;
                synchronized (deviceRejections) {
                    deviceRejections.put(dr.data.device, dr);
                }
                postChange(Change.DEVICE_REJECTED);
                break;
            } case FOLDER_REJECTED: {
                FolderRejected fr = (FolderRejected) e;
                synchronized (folderRejections) {
                    folderRejections.put(fr.data.folder + "-" + fr.data.device, fr);
                }
                postChange(Change.FOLDER_REJECTED);
                break;
            } case CONFIG_SAVED: {
                refreshConfig();
                break;
            } case DOWNLOAD_PROGRESS: {
                //TODO
                break;
            } case FOLDER_SUMMARY: {
                FolderSummary fs = (FolderSummary) e;
                updateModel(fs.data.folder, fs.data.summary);
                postChange(Change.MODEL, fs.data);
                break;
            } case FOLDER_COMPLETION: {
                FolderCompletion fc = (FolderCompletion) e;
                updateCompletion(fc.data.device, fc.data.folder, new Completion(fc.data.completion));
                postChange(Change.COMPLETION, fc.data);
                break;
            } case FOLDER_ERRORS: {
                break;
            } case ITEM_FINISHED: {
                break;
            } case ITEM_STARTED: {
                break;
            } case PING: {
                //refreshSystem();
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
                running = false;
                updateState(false);
                postChange(Change.FAILURE);
                break;
        }
    }

    boolean updateState(boolean online) {
        synchronized (lock) {
            //No state change dont eat event;
            if (this.online == online) return false;
            //New event came in while we are initializing, eat it
            if (online && onlineSub != null && !onlineSub.isUnsubscribed()) return true;
            if (online) {
                this.restarting = false;
                //Our online state depends on all these items
                //so we merge them together so we can defer
                //posting the ONLINE status until we have set
                //all the values, the extra toMap step
                //is to prevent sideeffects and synchronization errors
                onlineSub = Observable.merge(
                        restApi.system(),
                        restApi.config(),
                        restApi.configStatus(),
                        restApi.connections(),
                        restApi.deviceStats(),
                        restApi.folderStats(),
                        restApi.version(),
                        restApi.report()
                ).toMap(
                        Object::getClass
                ).subscribe(
                        (map) -> {
                            updateSystemInfo((SystemInfo) map.get(SystemInfo.class));
                            updateConfig((Config) map.get(Config.class));
                            updateConfigStats((ConfigStats) map.get(ConfigStats.class));
                            updateConnections((Connections) map.get(Connections.class));
                            setDeviceStats((DeviceStatsMap) map.get(DeviceStatsMap.class));
                            setFolderStats((FolderStatsMap) map.get(FolderStatsMap.class));
                            setVersion((Version) map.get(Version.class));
                            setReport((Report) map.get(Report.class));
                            synchronized (lock) {
                                this.online = true;
                            }
                        },
                        this::logException,
                        () -> {
                            setupPeriodicRefresh();
                            postChange(Change.ONLINE);
                            synchronized (lock) {
                                onlineSub = null;
                            }
                        }
                );
            } else {
                this.online = false;
                if (onlineSub != null) onlineSub.unsubscribe();
                cancelPeriodicRefresh();
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

    public void refreshSystem() {
        Subscription s = restApi.system()
                .subscribe(
                        this::updateSystemInfo,
                        this::logException,
                        () -> postChange(Change.SYSTEM)
                );
    }

    public void refreshConfig() {
        Subscription s = Observable.merge(
                restApi.config(),
                restApi.configStatus()
        ).toMap(
                Object::getClass
        ).subscribe(
                map -> {
                    updateConfig((Config) map.get(Config.class));
                    updateConfigStats((ConfigStats) map.get(ConfigStats.class));
                },
                this::logException,
                () -> postChange(Change.CONFIG_UPDATE)
        );
    }

    public void refreshConnections() {
        refreshConnections(false);
    }

    public void refreshConnections(boolean update) {
        Subscription s = restApi.connections()
                .subscribe(
                        this::updateConnections,
                        this::logException,
                        () -> postChange(update ? Change.CONNECTIONS_UPDATE : Change.CONNECTIONS_CHANGE));
    }

    public void refreshDeviceStats() {
        Subscription s = restApi.deviceStats()
                .subscribe(
                        this::setDeviceStats,
                        this::logException,
                        () -> postChange(Change.DEVICE_STATS)
                );
    }

    public void refreshFolderStats() {
        Subscription s = restApi.folderStats()
                .subscribe(
                        this::setFolderStats,
                        this::logException,
                        () -> postChange(Change.FOLDER_STATS)
                );
    }

    public void refreshVersion() {
        Subscription s = restApi.version()
                .subscribe(this::setVersion, this::logException);
    }

    public void refreshReport() {
        Subscription s = restApi.report()
                .subscribe(this::setReport, this::logException);
    }

    public void refreshFolder(String name) {
        Timber.d("refreshFolder(%s)", name);
        Subscription s = restApi.model(name)
                .subscribe(
                        model -> updateModel(name, model),
                        this::logException,
                        () -> postChange(Change.MODEL)
                );
    }

    public void refreshFolders(Collection<String> names) {
        Timber.d("refreshFolders()");
        if (names.isEmpty()) {
            return;
        }
        //Group all the network calls so we only receive one completion
        //event this will prevent change observers from receiving
        //mutliple MODEL changes
        List<Observable<Map.Entry<String, Model>>> observables = new LinkedList<>();
        for (String name: names) {
            observables.add(Observable.zip(Observable.just(name), restApi.model(name).first(), Pair::of));
        }
        Subscription s = Observable.merge(observables)
                .subscribe(
                        entry -> updateModel(entry.getKey(), entry.getValue()),
                        this::logException,
                        () -> postChange(Change.MODEL)
                );
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

    //Device,Folder pair
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


    void onLocalIndexUpdated(Event e) {
        synchronized (lock) {
            if (onLocalIndexUpdatedSubscription != null &&
                    !onLocalIndexUpdatedSubscription.isUnsubscribed()) {
                Timber.i("Ignoring LocalIndexUpdate... refresh in progress");
                return;
            }
            onLocalIndexUpdatedSubscription = Observable.timer(500, TimeUnit.MILLISECONDS, subscribeOn)
                    .subscribe(
                            ii -> refreshFolderStats(),
                            this::logException
                    );
        }
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

    public int getAnnounceServersTotal() {
        return systemInfo.get().announceServersTotal;
    }

    public List<String> getAnnounceServersFailed() {
        return systemInfo.get().announceServersFailed;
    }

    void updateSystemInfo(SystemInfo systemInfo) {
        systemInfo.announceServersTotal = 0;
        systemInfo.announceServersFailed.clear();
        if (systemInfo.extAnnounceOK != null) {
            systemInfo.announceServersTotal = systemInfo.extAnnounceOK.size();
            for (String server : systemInfo.extAnnounceOK.keySet()) {
                if (!systemInfo.extAnnounceOK.get(server)) {
                    systemInfo.announceServersFailed.add(server);
                }
            }
        }
        this.myId.set(systemInfo.myID);
        this.systemInfo.set(systemInfo);
    }

    public Config getConfig() {
        return config.get();
    }

    void updateConfig(Config config) {
        synchronized (folders) {
            folders.clear();
            for (FolderConfig f : config.folders) {
                folders.put(f.id, f);
            }
        }
        synchronized (devices) {
            devices.clear();
            devices.addAll(config.devices);
        }
        synchronized (folderRejections) {
            //remove any stale rejections
            Iterator<Map.Entry<String, FolderRejected>> ii = folderRejections.entrySet().iterator();
            while (ii.hasNext()) {
                if (getFolder(ii.next().getKey()) != null) {
                    ii.remove();
                }
            }
        }
        synchronized (deviceRejections) {
            //remove any stale rejections
            Iterator<Map.Entry<String, FolderRejected>> ii = folderRejections.entrySet().iterator();
            while (ii.hasNext()) {
                if (getDevice(ii.next().getKey()) != null) {
                    ii.remove();
                }
            }
        }
        this.config.set(config);
    }

    public boolean isConfigInSync() {
        return configInSync.get();
    }

    void updateConfigStats(ConfigStats configStats) {
        this.configInSync.set(configStats.configInSync);
    }

    public ConnectionInfo getConnection(String id) {
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
                if (!StringUtils.equals("total", key)) {
                    updateCompletionTotal(key, 100);
                }
            }
            connections.connections.clear();
            connections.connections.putAll(conns.connections);
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

    public Report getReport() {
        return report.get();
    }

    void setReport(Report report) {
        this.report.set(report);
    }

    @Nullable
    public Model getModel(String folderName) {
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

    public FolderConfig getFolder(String name) {
        synchronized (folders) {
            return folders.get(name);
        }
    }

    public Collection<FolderConfig> getFolders() {
        synchronized (folders) {
            return new ArrayList<>(folders.values());
        }
    }

    public List<DeviceConfig> getDevices() {
        synchronized (devices) {
            return new ArrayList<>(devices);
        }
    }

    @Nullable
    public DeviceConfig getThisDevice() {
        final String myid = myId.get();
        for (DeviceConfig d : getDevices()) {
            if (StringUtils.equals(d.deviceID, myid)){
                return d;
            }
        }
        return null;
    }

    @NonNull
    public List<DeviceConfig> getRemoteDevices() {
        final String myid = myId.get();
        List<DeviceConfig> dvs = new ArrayList<>();
        for (DeviceConfig d : getDevices()) {
            if (!StringUtils.equals(d.deviceID, myid)) {
                dvs.add(d);
            }
        }
        return dvs;
    }

    @Nullable
    public DeviceConfig getDevice(String deviceId) {
        for (DeviceConfig d : getDevices()) {
            if (StringUtils.equals(deviceId, d.deviceID)) {
                return d;
            }
        }
        return null;
    }

    void updateCompletion(String device, String folder, Completion comp) {
        Timber.d("Updating completion for %s", device);
        synchronized (completion) {
            if (!completion.containsKey(device)) {
                completion.put(device, new HashMap<String, Integer>());
            }
            completion.get(device).put(folder, Math.round(comp.completion));
            float tot = 0;
            int cnt = 0;
            for (String key : completion.get(device).keySet()) {
                if ("_total".equals(key)) continue;
                tot += completion.get(device).get(key);
                cnt++;
            }
            completion.get(device).put("_total", Math.min(100, Math.round(tot / cnt)));
        }
    }

    void updateCompletionTotal(String deviceId, int comp) {
        synchronized (completion) {
            if (!completion.containsKey(deviceId)) {
                completion.put(deviceId, new HashMap<String, Integer>());
            }
            completion.get(deviceId).put("_total", comp);
        }
    }

    public int getCompletionTotal(String deviceId) {
        synchronized (completion) {
            if (completion.containsKey(deviceId)) {
                return completion.get(deviceId).get("_total");
            } else {
                return -1;
            }
        }
    }

    @Nullable
    public Map<String, Integer> getCompletionStats(String deviceId) {
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

    public void refreshErrors() {
        Subscription s = restApi.errors()
                .subscribe(
                        errorsList::set,
                        this::logException,
                        () -> postChange(Change.NOTICE)
                );
    }

    public void clearErrors() {
        restApi.clearErrors().subscribe(
                v -> {
                },
                this::logException,
                () -> {
                    errorsList.set(null);
                    postChange(Change.NOTICE);
                });
    }

    @Nullable
    public GuiError getLatestError() {
        List<GuiError> errors = errorsList.get() != null ? errorsList.get().errors : null;
        if (errors != null && errors.size() > 0) {
            return errors.get(errors.size() - 1);
        } else {
            return null;
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
                        v -> {},
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
                        v -> {},
                        onError,
                        onComplete
                );
    }

    public void shareFolder(String name, String devId, Action1<Throwable> onError, Action0 onComplete) {
        restApi.config().zipWith(restApi.deviceId(devId),
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
                        v -> {
                        },
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
                                if (f.devices.isEmpty()) {
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
                        v -> {
                        },
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
                        v -> {},
                        onError,
                        onComplete
                );
    }

    public void ignoreDevice(String id, Action1<Throwable> onError, Action0 onComplete) {
        restApi.deviceId(id)
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
                        v -> {},
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
                        v -> {},
                        onError,
                        onComplete
                );
    }

    public void restart() {
        synchronized (lock) {
            restarting = true;
            eventMonitor.resetCounter();
            updateState(false);
        }
        restApi.restart().subscribe(v -> {
        }, this::logException);
    }

    public void shutdown() {
        //push to worker thread so we can stop event monitor
        final Scheduler.Worker worker = subscribeOn.createWorker();
        worker.schedule(new Action0() {
            @Override
            public void call() {
                synchronized (lock) {
                    restarting = false;
                    eventMonitor.stop();
                    updateState(false);
                }
                restApi.shutdown().subscribe(v -> {}, SessionController.this::logException);
                worker.unsubscribe();
            }
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

    public Subscription overrideChanges(String id, Action1<Throwable> onError) {
        return restApi.override(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (v) -> {},
                        onError
                );
    }

    public Subscription scanFolder(String id, Action1<Throwable> onError) {
        return restApi.scan(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (v) -> {
                        },
                        onError
                );
    }

    public Subscription getIgnores(String id, Action1<Ignores> onNext, Action1<Throwable> onError) {
        return restApi.ignores(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onNext,
                        onError
                );
    }

    public Subscription editIgnores(String id, Ignores ignores, Action1<Ignores> onNext, Action1<Throwable> onError, Action0 onComplete) {
        return restApi.updateIgnores(id, ignores)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onNext,
                        onError,
                        onComplete
                );
    }

    void setupPeriodicRefresh() {
        synchronized (lock) {
            cancelPeriodicRefresh();
            periodicRefreshSubscription = Observable.interval(30, TimeUnit.SECONDS, subscribeOn)
                    .subscribe(ii -> {
                        refreshSystem();
                        refreshConnections(true);
                        refreshErrors();
                    });
        }
    }

    void cancelPeriodicRefresh() {
        synchronized (lock) {
            if (periodicRefreshSubscription != null) {
                periodicRefreshSubscription.unsubscribe();
            }
        }
    }

    void logException(Throwable e) {
        Timber.e("%s: %s", e.getClass().getSimpleName(), e.getMessage(), e);
    }

    public Subscription subscribeChanges(Action1<ChangeEvent> onNext, Change... changes) {
        Observable<ChangeEvent> o;
        if (changes.length == 0) {
            o = changeBus.asObservable();
        } else {
            o = changeBus.asObservable()
                    .filter(c -> {
                        for (Change cc : changes) {
                            if (c.change == cc) return true;
                        }
                        return false;
                    });
        }
        //always post online event for new subscribers
        onNext.call(new ChangeEvent(online ? Change.ONLINE : Change.OFFLINE, null));
        return o.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext);
    }

}
