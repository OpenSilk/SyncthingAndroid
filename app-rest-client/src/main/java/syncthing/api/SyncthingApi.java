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

import org.joda.time.DateTime;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;
import syncthing.api.model.Completion;
import syncthing.api.model.Config;
import syncthing.api.model.ConfigStats;
import syncthing.api.model.Connections;
import syncthing.api.model.DeviceId;
import syncthing.api.model.DeviceStatsMap;
import syncthing.api.model.FolderStatsMap;
import syncthing.api.model.SystemErrors;
import syncthing.api.model.Ignores;
import syncthing.api.model.Model;
import syncthing.api.model.Need;
import syncthing.api.model.Ok;
import syncthing.api.model.Ping;
import syncthing.api.model.Report;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.SystemMessages;
import syncthing.api.model.Version;
import syncthing.api.model.event.Event;

/**
 * Created by drew on 3/1/15.
 */
public interface SyncthingApi {

    /**
     * The name of the HTTP header used for the syncthing API key.
     */
    public static final String HEADER_API_KEY = "X-API-Key";

    /*
     * GET
     */

    @GET("/rest/db/completion")
    Observable<Completion> completion(@Query("device") String device, @Query("folder") String folder);

    //@GET("/rest/db/file")

    @GET("/rest/db/ignores")
    Observable<Ignores> ignores(@Query("folder") String folder);

    @GET("/rest/db/need")
    Observable<Need> need(@Query("folder") String folder);

    @GET("/rest/db/status")
    Observable<Model> model(@Query("folder") String folder);

    //@GET("/rest/db/browse") //@Query("folder") ops @Query("prefix") @Query("disonly") @Query("levels")

    @GET("/rest/events")
    Observable<Event[]> events(@Query("since") long lastId);

    @GET("/rest/events")
    Observable<Event[]> events(@Query("since") long lastId, @Query("limit") int lim);

    @GET("/rest/stats/device")
    Observable<DeviceStatsMap> deviceStats();

    @GET("/rest/stats/folder")
    Observable<FolderStatsMap> folderStats();

    @GET("/rest/svc/deviceid")
    Observable<DeviceId> deviceId(@Query("id") String id);

    //@GET("/rest/svc/lang")

    @GET("/rest/svc/report")
    Observable<Report> report();

    @GET("/rest/system/browse")
    Observable<List<String>> autocompleteDirectory(@Query("current") String current);

    @GET("/rest/system/config")
    Observable<Config> config();

    @GET("/rest/system/config/insync")
    Observable<ConfigStats> configStatus();

    @GET("/rest/system/connections")
    Observable<Connections> connections();

    //@GET("/rest/system/discovery")

    @GET("/rest/system/error")
    Observable<SystemErrors> errors();

    @GET("/rest/system/ping")
    Observable<Ping> ping();

    @GET("/rest/system/status")
    Observable<SystemInfo> system();

    //@GET("/rest/system/upgrade")

    @GET("/rest/system/version")
    Observable<Version> version();

    //@GET("/rest/system/debug")

    @GET("/rest/system/log")
    Observable<SystemMessages> log();

    @GET("/rest/system/log")
    Observable<SystemMessages> log(@Query("since") DateTime since);

    //@GEE("/rest/system/log.txt") //op @Query("since")

    /*
     * POST
     */

    //@POST("/rest/db/prio")
    //Observable<Void> bump(@Query("folder") String folder, @Query("file") String file);

    @POST("/rest/db/ignores")
    Observable<Ignores> updateIgnores(@Query("folder") String folder, @Body Ignores ignores);

    @POST("/rest/db/override")
    Observable<Void> override(@Query("folder") String folder);

    @POST("/rest/db/scan")
    Observable<Void> scan(@Query("folder") String folder);

    @POST("/rest/db/scan")
    Observable<Void> scan(@Query("folder") String folder, @Query("sub") String subfolder);

    @POST("/rest/system/config")
    Observable<Config> updateConfig(@Body Config config);

    //@POST("/rest/system/discovery")
    //@POST("/rest/error")

    @POST("/rest/system/error/clear")
    Observable<Void> clearErrors();

    //@POST("/rest/system/ping")
    //@POST("/rest/system/reset")

    @POST("/rest/system/restart")
    Observable<Ok> restart();

    @POST("/rest/system/shutdown")
    Observable<Ok> shutdown();

    //@POST("/rest/system/upgrade")

    //@POST("/rest/system/pause") @Query("device")

    //@POST("/rest/system/resume") @Query("device")

    //@POST("/rest/system/debug") //op @Query("enable") @Query("disable")

    @POST("/rest/scan")
    Observable<Void> scan();

    /*
     * Misc
     */

    @GET("/qr/") //TODO proper image fetcher
    Observable<ResponseBody> qr(@Query("text") String id);

}
