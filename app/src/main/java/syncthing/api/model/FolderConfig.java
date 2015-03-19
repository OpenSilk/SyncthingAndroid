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
public class FolderConfig {
    public String id;
    public String path;
    public List<FolderDeviceConfig> devices = Collections.emptyList();
    public boolean readOnly;
    public int rescanIntervalS = 60;
    public boolean ignorePerms;
    public VersioningConfig versioning = new VersioningConfig();
    public boolean lenientMtimes;
    public int copiers = 1;
    public int pullers = 16;
    public int hashers = 0;
    public String invalid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderConfig folder = (FolderConfig) o;

        if (id != null ? !id.equals(folder.id) : folder.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
