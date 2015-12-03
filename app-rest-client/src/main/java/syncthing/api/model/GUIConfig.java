/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model;

import java.io.Serializable;

/**
 * Created by drew on 3/1/15.
 */
public class GUIConfig implements Serializable, Cloneable {
    private static final long serialVersionUID = -3114592263160879843L;
    public boolean enabled;
    public String address;
    public String user;
    public String password;
    public boolean useTLS;
    public String apiKey;
    public boolean insecureAdminAccess;

    public static GUIConfig withDefaults() {
        GUIConfig c = new GUIConfig();
        c.enabled = true;
        c.address = "127.0.0.1:8384";
        return c;
    }

    @Override
    public GUIConfig clone() {
        try {
            return (GUIConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw  new RuntimeException(e);
        }
    }
}
