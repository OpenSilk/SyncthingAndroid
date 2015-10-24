/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api;

import com.squareup.okhttp.ResponseBody;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.Query;
import rx.Observable;
import rx.Scheduler;
import syncthing.api.model.Completion;
import syncthing.api.model.Config;
import syncthing.api.model.ConfigStats;
import syncthing.api.model.Connections;
import syncthing.api.model.DeviceId;
import syncthing.api.model.DeviceStatsMap;
import syncthing.api.model.FolderStatsMap;
import syncthing.api.model.GuiErrors;
import syncthing.api.model.Ignores;
import syncthing.api.model.Model;
import syncthing.api.model.Need;
import syncthing.api.model.Ok;
import syncthing.api.model.Ping;
import syncthing.api.model.Report;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;
import syncthing.api.model.event.Event;

/**
 * Created by drew on 10/10/15.
 */
public class SynchingApiWrapper implements SyncthingApi {

    private final Scheduler scheduler;
    private final SyncthingApi api;

    private SynchingApiWrapper(Scheduler scheduler, SyncthingApi api) {
        this.scheduler = scheduler;
        this.api = api;
    }

    public static SyncthingApi wrap(SyncthingApi api, Scheduler scheduler) {
        return new SynchingApiWrapper(scheduler, api);
    }

    public static SyncthingApi unwrap(SyncthingApi api) {
        if (api instanceof SynchingApiWrapper) {
            return ((SynchingApiWrapper)api).api;
        } else {
            return api;
        }
    }

    @Override
    public Observable<Completion> completion(@Query("device") String device, @Query("folder") String folder) {
        return api.completion(device, folder).subscribeOn(scheduler);
    }

    @Override
    public Observable<Ignores> ignores(@Query("folder") String folder) {
        return api.ignores(folder).subscribeOn(scheduler);
    }

    @Override
    public Observable<Need> need(@Query("folder") String folder) {
        return api.need(folder).subscribeOn(scheduler);
    }

    @Override
    public Observable<Model> model(@Query("folder") String folder) {
        return api.model(folder).subscribeOn(scheduler);
    }

    @Override
    public Observable<Event[]> events(@Query("since") long lastId) {
        return api.events(lastId).subscribeOn(scheduler);
    }

    @Override
    public Observable<Event[]> events(@Query("since") long lastId, @Query("limit") int lim) {
        return api.events(lastId, lim).subscribeOn(scheduler);
    }

    @Override
    public Observable<DeviceStatsMap> deviceStats() {
        return api.deviceStats().subscribeOn(scheduler);
    }

    @Override
    public Observable<FolderStatsMap> folderStats() {
        return api.folderStats().subscribeOn(scheduler);
    }

    @Override
    public Observable<DeviceId> deviceId(@Query("id") String id) {
        return api.deviceId(id).subscribeOn(scheduler);
    }

    @Override
    public Observable<Report> report() {
        return api.report().subscribeOn(scheduler);
    }

    @Override
    public Observable<List<String>> autocompleteDirectory(@Query("current") String current) {
        return api.autocompleteDirectory(current).subscribeOn(scheduler);
    }

    @Override
    public Observable<Config> config() {
        return api.config().subscribeOn(scheduler);
    }

    @Override
    public Observable<ConfigStats> configStatus() {
        return api.configStatus().subscribeOn(scheduler);
    }

    @Override
    public Observable<Connections> connections() {
        return api.connections().subscribeOn(scheduler);
    }

    @Override
    public Observable<GuiErrors> errors() {
        return api.errors().subscribeOn(scheduler);
    }

    @Override
    public Observable<Ping> ping() {
        return api.ping().subscribeOn(scheduler);
    }

    @Override
    public Observable<SystemInfo> system() {
        return api.system().subscribeOn(scheduler);
    }

    @Override
    public Observable<Version> version() {
        return api.version().subscribeOn(scheduler);
    }

    @Override
    public Observable<Ignores> updateIgnores(@Query("folder") String folder, @Body Ignores ignores) {
        return api.updateIgnores(folder, ignores).subscribeOn(scheduler);
    }

    @Override
    public Observable<Void> override(@Query("folder") String folder) {
        return api.override(folder).subscribeOn(scheduler);
    }

    @Override
    public Observable<Void> scan(@Query("folder") String folder) {
        return api.scan(folder).subscribeOn(scheduler);
    }

    @Override
    public Observable<Void> scan(@Query("folder") String folder, @Query("sub") String subfolder) {
        return api.scan(folder,subfolder).subscribeOn(scheduler);
    }

    @Override
    public Observable<Config> updateConfig(@Body Config config) {
        return api.updateConfig(config).subscribeOn(scheduler);
    }

    @Override
    public Observable<Void> clearErrors() {
        return api.clearErrors().subscribeOn(scheduler);
    }

    @Override
    public Observable<Ok> restart() {
        return api.restart().subscribeOn(scheduler);
    }

    @Override
    public Observable<Ok> shutdown() {
        return api.shutdown().subscribeOn(scheduler);
    }

    @Override
    public Observable<Void> scan() {
        return api.scan().subscribeOn(scheduler);
    }

    @Override
    public Observable<ResponseBody> qr(@Query("text") String id) {
        return api.qr(id).subscribeOn(scheduler);
    }
}
