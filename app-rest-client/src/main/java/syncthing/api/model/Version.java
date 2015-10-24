/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
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
