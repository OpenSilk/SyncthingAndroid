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

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;

/**
 * Created by drew on 10/11/15.
 */
public class ConfigSaved extends Event<ConfigSaved.Data> {

    public ConfigSaved(long id, DateTime time, EventType type, Data data) {
        super(id, time, type, data);
    }

    public static class Data {
        @SerializedName("Version")
        public int version;
        @SerializedName("Folders")
        public List<FolderConfig> folders = Collections.emptyList();
        @SerializedName("Devices")
        public List<DeviceConfig> devices = Collections.emptyList();
        @SerializedName("GUI")
        public GUIConfig gui;
        @SerializedName("Options")
        public OptionsConfig options;
    }
}
