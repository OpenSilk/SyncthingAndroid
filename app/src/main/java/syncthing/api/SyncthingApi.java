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

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;
import syncthing.api.model.Completion;
import syncthing.api.model.Config;
import syncthing.api.model.ConfigStats;
import syncthing.api.model.ConnectionInfoMap;
import syncthing.api.model.Connections;
import syncthing.api.model.DeviceId;
import syncthing.api.model.DeviceStatsMap;
import syncthing.api.model.Event;
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

    //@GET("/rest/db/browse")

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
    Observable<GuiErrors> errors();

    @GET("/rest/system/ping")
    Observable<Ping> ping();

    @GET("/rest/system/status")
    Observable<SystemInfo> system();

    //@GET("/rest/system/upgrade")

    @GET("/rest/system/version")
    Observable<Version> version();

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

    @POST("/rest/scan")
    Observable<Void> scan();

    /*
     * Misc
     */

    @GET("/qr/") //TODO proper image fetcher
    Observable<Response> qr(@Query("text") String id);

}
