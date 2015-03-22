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

package syncthing.android.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.opensilk.common.dagger2.ForApplication;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by drew on 3/21/15.
 */
@Singleton
public class ServiceSettings {

    public static final String FILE_NAME = "service";

    public static final String ENABLED = "local_instance_enabled";

    public static final String RUN_WHEN = "run_when";
    public static final String WHEN_OPEN = "when_open";
    public static final String ALWAYS = "always";

    public static final String PERIODIC = "periodic";
    public static final String INTERVAL_HOURS = "periodic_interval_hours";

    public static final String SCHEDULED = "scheduled";
    public static final String RANGE_START = "scheduled_start";
    public static final String RANGE_END = "scheduled_end";

    public static final String ONLY_WIFI = "only_on_wifi";
    public static final String WIFI_NETWORKS = "TRANSIENT_wifi_networks";
    public static final String ONLY_CHARGING = "only_when_charging";

    final Context appContext;
    final ConnectivityManager cm;
    final WifiManager wm;
    final SharedPreferences prefs;

    @Inject
    public ServiceSettings(
            @ForApplication Context appContext,
            ConnectivityManager cm,
            WifiManager wm
    ) {
        this.appContext = appContext;
        this.cm = cm;
        this.wm = wm;
        this.prefs = appContext.getSharedPreferences(FILE_NAME, Context.MODE_MULTI_PROCESS);
    }

    boolean isDisabled() {
        return !prefs.getBoolean(ENABLED, true);
    }

    boolean isAllowedToRun() {
        if (isDisabled()) {
            Timber.d("isAllowedToRun(): SyncthingInstance disabled");
            return false;
        }
        boolean chargingOnly = prefs.getBoolean(ONLY_CHARGING, false);
        if (chargingOnly && !isCharging()) {
            Timber.d("isAllowedToRun(): chargingOnly=true and not charging... rejecting");
            return false;
        }
        if (!hasSuitableConnection()) {
            Timber.d("isAllowedToRun(): No suitable network... rejecting");
            return false;
        }
        String runWhen = prefs.getString(RUN_WHEN, WHEN_OPEN);
        if (ALWAYS.equals(runWhen)) {
            Timber.d("isAllowedToRun(): Always mate!");
            return true;
        } else if (SCHEDULED.equals(runWhen)) {
            long start = SyncthingUtils.parseTime(prefs.getString(RANGE_START, "00:00"));
            long end = SyncthingUtils.parseTime(prefs.getString(RANGE_END, "00:00"));
            boolean can = SyncthingUtils.isNowBetweenRange(start, end);
            Timber.d("isAllowedToRun(): is now a good time? %s", can);
            return can;
        } else /*runWhen == WHEN_OPEN*/ {
            Timber.d("isAllowedToRun(): nope!");
            return false;
        }
    }

    boolean isCharging() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battChanged = appContext.registerReceiver(null, filter);//stickies returned by register
        int status = battChanged != null ? battChanged.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
        return status != 0;
    }

    boolean hasSuitableConnection() {
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnectedOrConnecting()) {
            Timber.d("Not connected to any networks");
            return false;
        }
        boolean wifiOnly = prefs.getBoolean(ONLY_WIFI, true);
        if (wifiOnly && !isWifiOrEthernet(info.getType())) {
            Timber.d("Connection is not wifi network and wifiOnly=true");
            return false;
        }
        if (wifiOnly && !isConnectedToWhitelistedNetwork()) {
            Timber.d("Connected network not in whitelist wifiOnly=true");
            return false;
        }
        Timber.d("Connected network passes all preconditions");
        return true;
    }

    static boolean isWifiOrEthernet(int type) {
        return type == ConnectivityManager.TYPE_WIFI || type == ConnectivityManager.TYPE_ETHERNET;
    }

    boolean isConnectedToWhitelistedNetwork() {
        Set<String> whitelist = prefs.getStringSet(WIFI_NETWORKS, null);
        if (whitelist == null) {
            Timber.d("No whitelist found");
            return true;
        }
        WifiInfo info =  wm.getConnectionInfo();
        if (info == null) {
            Timber.w("WifiInfo was null");
            return true;
        }
        String ssid = info.getSSID();
        if (ssid == null) {
            Timber.w("SSID was null");
            return true;
        }
        if (whitelist.contains(ssid)) {
            Timber.d("Found %s in whitelist", ssid);
            return true;
        } else {
            return false;
        }
    }

    boolean isPeriodic() {
        return PERIODIC.equals(prefs.getString(RUN_WHEN, WHEN_OPEN));
    }

    boolean isOnSchedule() {
        return SCHEDULED.equals(prefs.getString(RUN_WHEN, WHEN_OPEN));
    }

    long getNextScheduledEndTime() {
        long start = SyncthingUtils.parseTime(prefs.getString(RANGE_START, "00:00"));
        long end = SyncthingUtils.parseTime(prefs.getString(RANGE_END, "00:00"));
        DateTime now = DateTime.now();
        Interval interval = SyncthingUtils.getIntervalForRange(now, start, end);
        if (interval.contains(now)) {
            //With scheduled range
            return interval.getEndMillis();
        } else {
            //Outside scheduled range, shutdown asap
            return now.getMillis() + AlarmManagerHelper.KEEP_ALIVE;
        }
    }

    long getNextScheduledStartTime() {
        long start = SyncthingUtils.parseTime(prefs.getString(RANGE_START, "00:00"));
        long end = SyncthingUtils.parseTime(prefs.getString(RANGE_END, "00:00"));
        DateTime now = DateTime.now();
        Interval interval = SyncthingUtils.getIntervalForRange(now, start, end);
        if (interval.isBefore(now)) {
            //Interval hasnt started yet
            return interval.getStartMillis();
        } else {
            //were either inside the interval or past it, get the next days start
            return interval.getStart().plusDays(1).getMillis();
        }
    }

}
