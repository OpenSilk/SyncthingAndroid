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

package syncthing.android.ui.session;

import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceStats;
import syncthing.api.model.Model;

/**
 * Created by drew on 3/20/15.
 */
public class Update {

    /*
     * Devices
     */

    //new device connection
    public static class ConnectionInfo {
        final String id;
        final syncthing.api.model.ConnectionInfo conn;

        public ConnectionInfo(String id, syncthing.api.model.ConnectionInfo conn) {
            this.id = id;
            this.conn = conn;
        }
    }

    public static class DeviceStats {
        final String id;
        final syncthing.api.model.DeviceStats stats;

        public DeviceStats(String id, syncthing.api.model.DeviceStats stats) {

            this.id = id;
            this.stats = stats;
        }
    }

    public static class Completion {
        final String id;
        final int comp;

        public Completion(String id, int comp) {
            this.id = id;
            this.comp = comp;
        }
    }

    /*
     * Folders
     */

    public static class Model {
        final String id;
        final syncthing.api.model.Model model;

        public Model(String id, syncthing.api.model.Model model) {
            this.id = id;
            this.model = model;
        }
    }

    //Cant extend, bus can't distinguish
    public static class ModelState /*extends Model*/ {
        final String id;
        final syncthing.api.model.Model model;

        public ModelState(String id, syncthing.api.model.Model model) {
            this.id = id;
            this.model = model;
        }
    }



}
