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
