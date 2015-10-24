/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model;

import java.util.Collections;
import java.util.List;

/**
 * Created by drew on 3/1/15.
 */
public class Config {
    public int version;
    public List<FolderConfig> folders = Collections.emptyList();
    public List<DeviceConfig> devices = Collections.emptyList();
    public GUIConfig gui;
    public OptionsConfig options;
    public List<String> ignoredDevices = Collections.emptyList();
}
