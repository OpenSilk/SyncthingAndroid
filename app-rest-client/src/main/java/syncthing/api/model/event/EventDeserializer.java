/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model.event;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

import syncthing.api.model.Config;

/**
 * Created by drew on 10/11/15.
 */
public class EventDeserializer implements JsonDeserializer<Event> {
    @Override
    public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Element was not an object");
        }
        JsonObject obj = json.getAsJsonObject();
        long id = context.deserialize(obj.get("id"), long.class);
        EventType type = context.deserialize(obj.get("type"), EventType.class);
        DateTime time = context.deserialize(obj.get("time"), DateTime.class);
        JsonElement data = obj.get("data");
        if (type == null) {
            type = EventType.UNKNOWN;
        }
        switch (type) {
            case CONFIG_SAVED: {
                return new ConfigSaved(id, time, type, context.deserialize(data, Config.class));
            }
            case DEVICE_CONNECTED: {
                return new DeviceConnected(id, time, type, context.deserialize(data, DeviceConnected.Data.class));
            }
            case DEVICE_DISCONNECTED: {
                return new DeviceDisconnected(id, time, type, context.deserialize(data, DeviceDisconnected.Data.class));
            }
            case DEVICE_DISCOVERED: {
                return new DeviceDiscovered(id, time, type, context.deserialize(data, DeviceDiscovered.Data.class));
            }
            case DEVICE_PAUSED: {
                return new DevicePaused(id, time, type, context.deserialize(data, DevicePaused.Data.class));
            }
            case DEVICE_REJECTED: {
                return new DeviceRejected(id, time, type, context.deserialize(data, DeviceRejected.Data.class));
            }
            case DEVICE_RESUMED: {
                return new DeviceResumed(id, time, type, context.deserialize(data, DeviceResumed.Data.class));
            }
            case DOWNLOAD_PROGRESS: {
                return new DownloadProgress(id, time, type, context.deserialize(data, DownloadProgress.Data.class));
            }
            case FOLDER_COMPLETION: {
                return new FolderCompletion(id, time, type, context.deserialize(data, FolderCompletion.Data.class));
            }
            case FOLDER_ERRORS: {
                return new FolderErrors(id, time, type, context.deserialize(data, FolderErrors.Data.class));
            }
            case FOLDER_REJECTED: {
                return new FolderRejected(id, time, type, context.deserialize(data, FolderRejected.Data.class));
            }
            case FOLDER_SCAN_PROGRESS: {
                return new FolderScanProgress(id, time, type, context.deserialize(data, FolderScanProgress.Data.class));
            }
            case FOLDER_SUMMARY: {
                return new FolderSummary(id, time, type, context.deserialize(data, FolderSummary.Data.class));
            }
            case ITEM_FINISHED: {
                return new ItemFinished(id, time, type, context.deserialize(data, ItemFinished.Data.class));
            }
            case ITEM_STARTED: {
                return new ItemStarted(id, time, type, context.deserialize(data, ItemStarted.Data.class));
            }
            case LOCAL_INDEX_UPDATED: {
                return new LocalIndexUpdated(id, time, type, context.deserialize(data, LocalIndexUpdated.Data.class));
            }
            case PING: {
                return new Ping(id, time, type);
            }
            case REMOTE_INDEX_UPDATED: {
                return new RemoteIndexUpdated(id, time, type, context.deserialize(data, RemoteIndexUpdated.Data.class));
            }
            case STARTING: {
                return new Starting(id, time, type, context.deserialize(data, Starting.Data.class));
            }
            case STARTUP_COMPLETE: {
                return new StartupComplete(id, time, type);
            }
            case STATE_CHANGED: {
                return new StateChanged(id, time, type, context.deserialize(data, StateChanged.Data.class));
            }
            case RELAY_STATE_CHANGED: {
                return new RelayStateChanged(id, time, type, context.deserialize(data, RelayStateChanged.Data.class));
            }
            default: {
                return new UnknownEvent(id, time, obj.toString());
            }
        }
    }
}
