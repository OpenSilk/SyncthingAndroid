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

import org.joda.time.DateTime;

/**
 * Created by drew on 3/1/15.
 */
public class Model {
    public long globalBytes;
    public long globalDeleted;
    public long globalFiles;
    public long localBytes;
    public long localDeleted;
    public long localFiles;
    public long inSyncBytes;
    public long inSyncFiles;
    public long needBytes;
    public long needFiles;
    public ModelState state;
    public String invalid;
    public DateTime stateChanged;
    public boolean ignorePatterns;
    public long version;
}
