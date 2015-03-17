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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.EMPTY_SET;

/**
 * Created by drew on 3/5/15.
 */
public class DeviceStatsMap extends LinkedHashMap<String, DeviceStats> {
    public static final DeviceStatsMap EMPTY = new DeviceStatsMap() {
        @Override public boolean containsKey(Object key) {
            return false;
        }
        @Override public boolean containsValue(Object value) {
            return false;
        }
        @Override public Set entrySet() {
            return EMPTY_SET;
        }
        @Override public DeviceStats get(Object key) {
            return null;
        }
        @Override public Set keySet() {
            return EMPTY_SET;
        }
        @Override public Collection values() {
            return EMPTY_LIST;
        }
        private Object readResolve() {
            return EMPTY_MAP;
        }
    };
}
