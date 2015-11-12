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

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.zeroturnaround.zip.ZipUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import syncthing.android.R;
import syncthing.api.model.DeviceConfig;
import timber.log.Timber;

/**
 * Created by drew on 3/8/15.
 */
public class SyncthingUtils {

    private static int sForegroundActivities;

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

    public static String getSyncthingCACert(Context context) {
        try {
            return readFile(new File(context.getApplicationContext().getFilesDir(), "st-config/https-cert.pem"));
        } catch (IOException e) {
            Timber.e("Failed to retrieve CA Cert", e);
            return null;
        }
    }

    private static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");
        while((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        reader.close();
        return stringBuilder.toString();
    }

    public static String getSyncthingBinaryPath(Context context) {
        return new File(context.getApplicationContext().getFilesDir(), "syncthing.bin").getAbsolutePath();
    }

    public static String getSyncthingInotifyBinaryPath(Context context) {
        return new File(context.getApplicationContext().getFilesDir(), "syncthing-inotify.bin").getAbsolutePath();
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
                return deviceId.substring(0, 6).toUpperCase(Locale.US);
            } else {
                return deviceId.toUpperCase(Locale.US);
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
        if (strings == null || strings.length == 0) {
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

    public static String generateName(boolean dashed) {
        String name = Build.MODEL.replaceAll("[^a-zA-Z0-9 ]", "");
        if (name.startsWith("Android SDK built for"))
            name = "Nexus One";
        String split[] = name.split(" ");
        name = split[0];
        for (int i = 1; i < split.length; i++) {
            if (name.length() + split[i].length() > 20)
                break;
            name += (dashed ? "-" : " ") + split[i];
        }
        return name;
    }

    public static String generateDeviceName(boolean dashed) {
        return generateName(dashed);
    }

    public static String generateUsername() {
        return generateName(false);
    }

    public static String generatePassword() {
        return randomString(20);
    }

    public static String hiddenString(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
            sb.append("*");
        return sb.toString();
    }

    public static String randomString(int len) {
        PRNGFixes.apply();
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < len; i++)
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        return sb.toString();
    }

    public static Interval getIntervalForRange(DateTime now, long start, long end) {
        DateTime daybreak = now.withTimeAtStartOfDay();
        Interval interval;
        if (start < end) {
            //same day
            interval = new Interval(daybreak.plus(start), daybreak.plus(end));
        } else /*start >|== end*/ {
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

    public static File[] listExportedConfigs(Context context) {
        File root = Environment.getExternalStorageDirectory();
        return root.listFiles((dir, filename) -> StringUtils.startsWith(filename, context.getPackageName() + "-export")
                && StringUtils.endsWith(filename, ".zip"));
    }

    public static void exportConfig(Context context) {
        File configDir = getConfigDirectory(context);
        if (!configDir.exists()) {
            Toast.makeText(context, R.string.no_config_found, Toast.LENGTH_LONG).show();
            return;
        }
        File zipFile = new File(Environment.getExternalStorageDirectory(),
                context.getPackageName() + "-export-"
                        + DateTime.now().toString("yyyy-MM-dd--HH-mm-ss") + ".zip");
        if (zipFile.exists()) {
            return;//Double click or something. just ignore
        }
        File tmpDir = new File(context.getApplicationContext().getCacheDir(), randomString(6));
        try {
            //copy the files we care about into tmp location
            File[] files = configDir.listFiles((dir, filename) -> filename.endsWith(".xml") || filename.endsWith(".pem"));
            for (File f : files) {
                FileUtils.copyFileToDirectory(f, tmpDir);
            }
            ZipUtil.pack(tmpDir, zipFile);
            new AlertDialog.Builder(context)
                    .setTitle(R.string.archive_created)
                    .setMessage(context.getString(R.string.archive_at_location, zipFile.getAbsolutePath()))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } catch (IOException|RuntimeException e) {
            FileUtils.deleteQuietly(zipFile);
            Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
            Timber.e("Failed to export", e);
        } finally {
            FileUtils.deleteQuietly(tmpDir);
        }
    }

    public static void importConfig(Context context, Uri uri, boolean force) {
        File configDir = getConfigDirectory(context);
        if (configDir.exists()) {
            if (!force) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.overwrite)
                        .setMessage(R.string.overwrite_current_config)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> importConfig(context, uri, true))
                        .show();
                return;
            } else {
                context.startService(new Intent(context, SyncthingInstance.class).setAction(SyncthingInstance.SHUTDOWN));
                try {
                    FileUtils.cleanDirectory(configDir);
                } catch (IOException e) {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                    Timber.e("Failed to import", e);
                    return;
                }
            }
        }
        InputStream is = null;
        try {
            //TODO copy zip to temp location and check if its a valid config
            is = context.getContentResolver().openInputStream(uri);
            ZipUtil.unpack(is, configDir);
            File[] files = configDir.listFiles();
            for (File f : files) {
                Runtime.getRuntime().exec("chmod 0600 " + f.getAbsolutePath()).waitFor();
                Timber.d("chmod 0600 on %s", f.getAbsolutePath());
            }
            Toast.makeText(context, R.string.config_imported, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            FileUtils.deleteQuietly(configDir);
            Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static boolean isClipBoardSupported(Context context) {
        ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        return clipboard != null;
    }

    public static void copyToClipboard(Context context, CharSequence label, String id) {
        ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, id);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    public static void shareDeviceId(Context context, String id) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, id);
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)));
    }

    private static final Pattern ipv4Pattern;
    private static final Pattern ipv4PatternPort;
    //private static final Pattern ipv4PatternPortPath;
    private static final Pattern ipv6Pattern;
    private static final Pattern ipv6PatternPort;
    //private static final Pattern ipv6PatternPortPath;
    private static final Pattern domainNamePattern;
    private static final Pattern domainNamePatternPort;
    private static final Pattern domainNamePatternPath;
    //private static final Pattern domainNamePatternPortPath;
    static {
        try {
            ipv4Pattern = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])", Pattern.CASE_INSENSITIVE);
            ipv4PatternPort = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])(:([1-9]|[1-9]\\d{1,3}|[1-3]\\d{4}|4[0-8]\\d{3}|490\\d{2}|491[0-4]\\d|49150))", Pattern.CASE_INSENSITIVE);
            ipv6Pattern = Pattern.compile("([0-9a-f]{1,4})(:([0-9a-f]){1,4}){7}", Pattern.CASE_INSENSITIVE);
            ipv6PatternPort = Pattern.compile("(\\[)([0-9a-f]{1,4})(:([0-9a-f]){1,4}){7}(\\])(:([1-9]|[1-9]\\d{1,3}|[1-3]\\d{4}|4[0-8]\\d{3}|490\\d{2}|491[0-4]\\d|49150))", Pattern.CASE_INSENSITIVE);
            //TODO support abbreviated form
            domainNamePattern = Pattern.compile("((?!-)[a-z0-9-]{1,63}(?<!-)\\.)+([a-z]{2,6})(/)?", Pattern.CASE_INSENSITIVE);
            domainNamePatternPort = Pattern.compile("((?!-)[a-z0-9-]{1,63}(?<!-)\\.)+([a-z]{2,6})(:([1-9]|[1-9]\\d{1,3}|[1-3]\\d{4}|4[0-8]\\d{3}|490\\d{2}|491[0-4]\\d|49150))", Pattern.CASE_INSENSITIVE);
            domainNamePatternPath = Pattern.compile("((?!-)[a-z0-9-]{1,63}(?<!-)\\.)+([a-z]{2,6})(/.+)", Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isIpAddress(String ipAddress) {
        if (ipv4Pattern.matcher(ipAddress).matches()) {
            return true;
        }
        if (ipv6Pattern.matcher(ipAddress).matches()) {
            return true;
        }
        //just assume the user input a valid ipv6 addr
        return StringUtils.countMatches(ipAddress, "::") > 0;
    }

    public static boolean isIpAddressWithPort(String ipAddress) {
        if (ipv4PatternPort.matcher(ipAddress).matches()) {
            return true;
        }
        if (ipv6PatternPort.matcher(ipAddress).matches()) {
            return true;
        }
        //just assume the user input a valid ipv6 addr
        return StringUtils.countMatches(ipAddress, "::") > 0;
    }

    public static boolean isDomainName(String hostName) {
        return domainNamePattern.matcher(hostName).matches();
    }

    public static boolean isDomainNameWithPort(String hostName) {
        return domainNamePatternPort.matcher(hostName).matches();
    }

    public static boolean isDomainNameWithPath(String hostname) {
        return domainNamePatternPath.matcher(hostname).matches();
    }

    public static String extractHost(String uri) {
        return Uri.parse(uri).getHost();
    }

    public static String extractPort(String uri) {
        return String.valueOf(Uri.parse(uri).getPort());
    }

    public static boolean isHttps(String uri) {
        return StringUtils.startsWithIgnoreCase(uri, "https");
    }

    public static String buildUrl(@NonNull String host, @NonNull String port, boolean tls) {
        host = stripHttp(StringUtils.trim(host).toLowerCase(Locale.US));
        port = StringUtils.strip(StringUtils.trim(port), ":");
        String path = "";
        if (isDomainNameWithPath(host)) {
            path = Uri.parse("http://" + host).getPath();
            host = StringUtils.remove(host, path);
        }
        return (tls ? "https://" : "http://") + host + ":" + port +
                //without the trailing slash retrofit wont build the url correctly
                ((StringUtils.isEmpty(path) || StringUtils.endsWith(path, "/")) ? path : (path + "/"));
    }

    private static String stripHttp(String uri) {
        if (StringUtils.startsWithAny(uri, "http://", "https://")) {
            uri = StringUtils.remove(uri, "http://");
            uri = StringUtils.remove(uri, "https://");
        }
        return uri;
    }

    public static String buildAuthorization(String user, String pass) {
        return "Basic " + Base64.encodeToString((user + ":" + pass).getBytes(), 0);
    }
}
