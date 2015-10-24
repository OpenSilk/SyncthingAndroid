/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
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
public class FolderStatsMap extends LinkedHashMap<String, FolderStats> {
    public static final FolderStatsMap EMPTY = new FolderStatsMap() {
        private static final long serialVersionUID = -7495908676496328322L;
        @Override public boolean containsKey(Object key) {
            return false;
        }
        @Override public boolean containsValue(Object value) {
            return false;
        }
        @Override public Set entrySet() {
            return EMPTY_SET;
        }
        @Override public FolderStats get(Object key) {
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
    private static final long serialVersionUID = -2891599138746539535L;
}
