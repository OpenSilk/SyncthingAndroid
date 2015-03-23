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

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.WeakHashMap;

import syncthing.android.R;
import syncthing.api.model.DeviceConfig;
import timber.log.Timber;

/**
 * Created by drew on 3/8/15.
 */
public class SyncthingUtils {

    //private static ISyncthingInstance sService;
    private static int sForegroundActivities;
    private static final WeakHashMap<Context, ServiceBinder> sConnectionMap;

    static {
        sConnectionMap = new WeakHashMap<>();
    }

    /**
     * @param context The {@link Context} to use
     * @param callback The {@link ServiceConnection} to use
     * @return The new instance of {@link ServiceToken}
     */
    public static ServiceToken bindToService(final Context context, final ServiceConnection callback) {
        final ContextWrapper contextWrapper;
        if (context instanceof Activity) {
            Activity realActivity = ((Activity)context).getParent();
            if (realActivity == null) {
                realActivity = (Activity) context;
            }
            contextWrapper = new ContextWrapper(realActivity);
        } else {
            contextWrapper = new ContextWrapper(context);
        }
        contextWrapper.startService(new Intent(contextWrapper, SyncthingInstance.class));
        final ServiceBinder binder = new ServiceBinder(callback);
        if (contextWrapper.bindService(new Intent(contextWrapper, SyncthingInstance.class), binder, 0)) {
            sConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    /**
     * @param token The {@link ServiceToken} to unbind from
     */
    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper contextWrapper = token.mWrappedContext;
        final ServiceBinder binder = sConnectionMap.remove(contextWrapper);
        if (binder == null) {
            return;
        }
        contextWrapper.unbindService(binder);
        if (sConnectionMap.isEmpty()) {
            //sService = null;
        }
    }

    public static class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;

        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            //sService = SyncthingInstanceBinder.asInterface (service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            //sService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    /**
     * Used to build and show a notification when Syncthing is sent into the background
     *
     * @param context The {@link Context} to use.
     */
    public static void notifyForegroundStateChanged(final Context context, boolean inForeground) {
        int old = sForegroundActivities;
        if (inForeground) {
            sForegroundActivities++;
        } else {
            sForegroundActivities--;
        }

        if (old == 0 || sForegroundActivities == 0) {
            final Intent intent = new Intent(context, SyncthingInstance.class);
            intent.setAction(SyncthingInstance.FOREGROUND_STATE_CHANGED);
            intent.putExtra(SyncthingInstance.EXTRA_NOW_IN_FOREGROUND, sForegroundActivities != 0);
            context.startService(intent);
        }
    }

    public static File getConfigDirectory(Context context) {
        return new File(context.getApplicationContext().getFilesDir(), "st-config");
    }

    public static String getGoBinaryPath(Context context) {
        return new File(context.getApplicationContext().getFilesDir(), "syncthing.bin").getAbsolutePath();
    }

    /*
     * UTILS
     */

    public static String getDisplayName(DeviceConfig device) {
        if (!StringUtils.isEmpty(device.name)) {
            return device.name;
        } else {
            return truncateId(device.deviceID);
        }
    }

    public static String truncateId(String deviceId) {
        if (!StringUtils.isEmpty(deviceId)) {
            if (deviceId.length() >= 6) {
                return deviceId.substring(0, 6).toUpperCase();
            } else {
                return deviceId.toUpperCase();
            }
        } else {
            return "[unknown]";
        }
    }

    private static final DecimalFormat READABLE_DECIMAL_FORMAT = new DecimalFormat("#,##0.#");
    private static final CharSequence UNITS = "KMGTPE";

    //http://stackoverflow.com/a/3758880
    public static String humanReadableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format(Locale.US, "%s %siB",
                READABLE_DECIMAL_FORMAT.format(bytes / Math.pow(1024, exp)), UNITS.charAt(exp - 1));
    }

    public static String humanReadableTransferRate(long bytes) {
        return humanReadableSize(bytes) + "/s";
    }

    public static String daysToSeconds(String days) {
        return String.valueOf(Long.decode(days) * 86400);
    }

    public static String secondsToDays(String seconds) {
        return String.valueOf(Long.decode(seconds) / 86400);
    }

    public static String[] rollArray(String string) {
        return StringUtils.split(string, " ,");
    }

    public static String unrollArray(String[] strings) {
        StringBuilder b = new StringBuilder(50);
        if (strings.length == 0) {
            return null;
        }
        for (int ii=0; ii<strings.length; ii++) {
            b.append(strings[ii]);
            if (ii+1 < strings.length) {
                b.append(",");
            }
        }
        return b.toString();
    }

    private static final CharSequence CHARS = "01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";

    public static String randomString(int len) {
        String res = "";
        for (int ii=0; ii<len; ii++) {
            res += CHARS.charAt(Math.round((float)(Math.random() * (CHARS.length() - 1))));
        }
        return res;
    }

    public static Interval getIntervalForRange(DateTime now, long start, long end) {
        DateTime daybreak = now.withTimeAtStartOfDay();
        Interval interval;
        if (start == end) {
            Timber.w("start and end ranges are the same!");
            interval = new Interval(daybreak.plus(start), daybreak.plus(end));
        } else if (start < end) {
            //same day
            interval = new Interval(daybreak.plus(start), daybreak.plus(end));
        } else /*start > end*/ {
            if (now.isAfter(daybreak.plus(start))) {
                //rolls next day
                interval = new Interval(daybreak.plus(start), daybreak.plusDays(1).plus(end));
            } else {
                //rolls previous day
                interval = new Interval(daybreak.minusDays(1).plus(start), daybreak.plus(end));
            }
        }
        return interval;
    }

    public static boolean isNowBetweenRange(long start, long end) {
        DateTime now = DateTime.now();
        return getIntervalForRange(now, start, end).contains(now);
    }

    public static long parseTime(String str) {
        String[] split = StringUtils.split(str, ":");
        int hour = Integer.decode(split[0]);
        int min = Integer.decode(split[1]);
        return hoursToMillis(hour) + minutesToMillis(min);
    }

    public static long hoursToMillis(int hours) {
        return (long) hours * 3600000L;
    }

    public static long minutesToMillis(int minutes) {
        return (long) minutes * 60000L;
    }

    public static void copyDeviceId(Context context, String id) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(id);
        } else {
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.device_id), id);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(context, R.string.device_id_copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

}
