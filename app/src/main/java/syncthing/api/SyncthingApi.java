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
import syncthing.api.model.DeviceId;
import syncthing.api.model.DeviceStatsMap;
import syncthing.api.model.Event;
import syncthing.api.model.FolderStatsMap;
import syncthing.api.model.GuiErrors;
import syncthing.api.model.Ignores;
import syncthing.api.model.Model;
import syncthing.api.model.Need;
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

    @GET("/rest/ping")
    Observable<Ping> ping();

    @GET("/rest/completion")
    Observable<Completion> completion(@Query("device") String device, @Query("folder") String folder);

    @GET("/rest/config")
    Observable<Config> config();

    @GET("/rest/config/sync")
    Observable<ConfigStats> configStatus();

    @GET("/rest/connections")
    Observable<ConnectionInfoMap> connections();

    @GET("/rest/autocomplete/directory")
    Observable<List<String>> autocompleteDirectory(@Query("current") String current);

    //@GET("/rest/discovery")

    @GET("/rest/errors")
    Observable<GuiErrors> errors();

    @GET("/rest/events")
    Observable<Event[]> events(@Query("since") long lastId);

    @GET("/rest/events")
    Observable<Event[]> events(@Query("since") long lastId, @Query("limit") int lim);

    @GET("/rest/ignores")
    Observable<Ignores> ignores(@Query("folder") String folder);

    //@GET("/rest/lang")

    @GET("/rest/model")
    Observable<Model> model(@Query("folder") String folder);

    @GET("/rest/need")
    Observable<Need> need();

    @GET("/rest/need")
    Observable<Need> need(@Query("folder") String folder);

    @GET("/rest/deviceid")
    Observable<DeviceId> deviceId(@Query("id") String id);

    @GET("/rest/report")
    Observable<Report> report();

    @GET("/rest/system")
    Observable<SystemInfo> system();

    //@GET("/rest/upgrade")

    @GET("/rest/version")
    Observable<Version> version();

    @GET("/rest/stats/device")
    Observable<DeviceStatsMap> deviceStats();

    @GET("/rest/stats/folder")
    Observable<FolderStatsMap> folderStats();

    //@GET("/rest/filestatus")
    //fileStatus(@Query("folder") String folder, @Query("file") String file);

    /*
     * POST
     */

    //@POST("/rest/ping")

    @POST("/rest/config")
    Observable<Void> updateConfig(@Body Config config);

    //@POST("/rest/discovery/hint")

    //@POST("/rest/error")

    @POST("/rest/error/clear")
    Observable<Void> clearErrors();

    //@POST("/rest/ignores")

    @POST("/rest/model/override")
    Observable<Void> override(@Query("folder") String folder);

    //@POST("/rest/reset")

    @POST("/rest/restart")
    Observable<Void> restart();

    @POST("/rest/shutdown")
    Observable<Void> shutdown();

    //@POST("/rest/upgrade")

    @POST("/rest/scan")
    Observable<Void> scan();

    @POST("/rest/scan")
    Observable<Void> scan(@Query("folder") String folder);

    @POST("/rest/scan")
    Observable<Void> scan(@Query("folder") String folder, @Query("sub") String subfolder);

    @POST("/rest/bump")
    Observable<Void> bump(@Query("folder") String folder);

    /*
     * Misc
     */

    @GET("/qr/") //TODO proper image fetcher
    Observable<Response> qr(@Query("text") String id);

}
