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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by drew on 3/1/15.
 */
public class SystemInfo {
    public long alloc;
    public double cpuPercent;
    public int goroutines;
    public String myID;
    public long sys;
    public String pathSeparator;
    public int uptime;
    public String tilde;
    public Map<String, Boolean> extAnnounceOK = Collections.emptyMap();
    //pojo
    public transient int announceServersTotal;
    public transient List<String> announceServersFailed = new ArrayList<>();
}
