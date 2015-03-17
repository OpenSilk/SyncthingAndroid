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

package syncthing.api.model;

import com.google.gson.annotations.SerializedName;

/**
* Created by drew on 3/17/15.
*/
public enum EventType {
    @SerializedName("Starting") STARTING,
    @SerializedName("StartupComplete") STARTUP_COMPLETE,
    @SerializedName("Ping") PING,
    @SerializedName("DeviceDiscovered") DEVICE_DISCOVERED,
    @SerializedName("DeviceConnected") DEVICE_CONNECTED,
    @SerializedName("DeviceDisconnected") DEVICE_DISCONNECTED,
    @SerializedName("RemoteIndexUpdated") REMOTE_INDEX_UPDATED,
    @SerializedName("LocalIndexUpdated") LOCAL_INDEX_UPDATED,
    @SerializedName("ItemStarted") ITEM_STARTED,
    @SerializedName("ItemFinished") ITEM_FINISHED,
    @SerializedName("StateChanged") STATE_CHANGED,
    @SerializedName("FolderRejected") FOLDER_REJECTED,
    @SerializedName("DeviceRejected") DEVICE_REJECTED,
    @SerializedName("ConfigSaved") CONFIG_SAVED,
    @SerializedName("DownloadProgress") DOWNLOAD_PROGRESS,
}
