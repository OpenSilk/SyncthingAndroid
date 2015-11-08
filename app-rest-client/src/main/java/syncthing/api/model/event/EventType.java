/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model.event;

import com.google.gson.annotations.SerializedName;

/**
* Created by drew on 3/17/15.
*/
public enum EventType {
    @SerializedName("Ping") PING,
    @SerializedName("Starting") STARTING,
    @SerializedName("StartupComplete") STARTUP_COMPLETE,
    @SerializedName("DeviceDiscovered") DEVICE_DISCOVERED,
    @SerializedName("DeviceConnected") DEVICE_CONNECTED,
    @SerializedName("DeviceDisconnected") DEVICE_DISCONNECTED,
    @SerializedName("DevicePaused") DEVICE_PAUSED,
    @SerializedName("DeviceRejected") DEVICE_REJECTED,
    @SerializedName("DeviceResumed") DEVICE_RESUMED,
    @SerializedName("LocalIndexUpdated") LOCAL_INDEX_UPDATED,
    @SerializedName("RemoteIndexUpdated") REMOTE_INDEX_UPDATED,
    @SerializedName("ItemStarted") ITEM_STARTED,
    @SerializedName("ItemFinished") ITEM_FINISHED,
    @SerializedName("StateChanged") STATE_CHANGED,
    @SerializedName("FolderRejected") FOLDER_REJECTED,
    @SerializedName("ConfigSaved") CONFIG_SAVED,
    @SerializedName("DownloadProgress") DOWNLOAD_PROGRESS,
    @SerializedName("FolderSummary") FOLDER_SUMMARY,
    @SerializedName("FolderCompletion") FOLDER_COMPLETION,
    @SerializedName("FolderErrors") FOLDER_ERRORS,
    @SerializedName("RelayStateChanged") RELAY_STATE_CHANGED,
    @SerializedName("FolderScanProgress") FOLDER_SCAN_PROGRESS,
    UNKNOWN,
}
