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

package syncthing.android.ui.login;


import android.util.Base64;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * Created by drew on 3/12/15.
 */
public class LoginUtils {

    public static String extractHost(String uri) {
        uri = stripHttp(uri);
        return StringUtils.substring(uri, 0, StringUtils.lastIndexOf(uri, ":"));
    }

    public static String extractPort(String uri) {
        return StringUtils.substring(uri, StringUtils.lastIndexOf(uri, ":")+1);
    }

    public static boolean isHttps(String uri) {
        return StringUtils.startsWith(uri.toLowerCase(Locale.US), "https");
    }

    public static boolean validateHost(String host) {
        if (StringUtils.isEmpty(host)) {
            return false;
        }
        return true;
    }

    public static boolean validatePort(String port) {
        if (StringUtils.isEmpty(port)) {
            return false;
        }
        if (!StringUtils.isNumeric(port)) {
            return false;
        }
        return true;
    }

    public static String buildUrl(String host, String port, boolean tls) {
        String realUrl = StringUtils.trim(host).toLowerCase(Locale.US);
        realUrl = stripHttp(realUrl);
        String realPort = StringUtils.trim(port);
        realPort = StringUtils.strip(realPort, ":");
        return (tls ? "https://" : "http://") + realUrl + ":" + realPort;
    }

    static String stripHttp(String uri) {
        if (StringUtils.startsWithAny(uri, "http://", "https://")) {
            uri = StringUtils.remove(uri, "http://");
            uri = StringUtils.remove(uri, "https://");
        }
        return uri;
    }

    public static String buildAuthorization(String user, String pass) {
        return "Basic " + Base64.encodeToString((user+":"+pass).getBytes(), 0);
    }

}
