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

import java.util.Locale;

/**
 * Created by drew on 3/1/15.
 */
public class Version {
    public String longVersion;
    public String version;
    public String arch;
    public String os;

    @Override
    public String toString() {
        return String.format(Locale.US, "%s on %s (%s)", version, readableOS(), readableArch());
    }

    private String readableOS() {
        switch (os) {
            case "darwin":
                return "Mac OS X";
            case "freebsd":
                return "FreeBSD";
            case "openbsd":
                return "OpenBSD";
            case "netbsd":
                return "NetBSD";
            case "linux":
                return "Linux";
            case "windows":
                return "Windows";
            case "solaris":
                return "Solaris";
            case "android":
                return "Android";
            default:
                return os;
        }
    }

    private String readableArch() {
        switch (arch) {
            case "386":
                return "32 bit";
            case "amd64":
                return "64 bit";
            case "arm":
                return "ARM";
            default:
                return arch;
        }
    }
}
