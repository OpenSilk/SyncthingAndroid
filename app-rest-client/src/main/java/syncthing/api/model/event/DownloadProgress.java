/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model.event;

import org.joda.time.DateTime;

import java.util.HashMap;

/**
 * Created by drew on 10/11/15.
 */
public class DownloadProgress extends Event<DownloadProgress.Data> {
    public DownloadProgress(long id, DateTime time, EventType type, Data data) {
        super(id, time, type, data);
    }

    public static class Data extends HashMap<String, Files> {
        private static final long serialVersionUID = -2642445567671314630L;
    }

    public static class Files extends HashMap<String, Progress> {
        private static final long serialVersionUID = 1539569407920748984L;
    }

    public static class Progress {
        public int total;
        public int reused;
        public int copiedFromOrigin;
        public int copiedFromElsewhere;
        public int pulled;
        public int pulling;
        public long bytesDone;
        public long bytesTotal;
    }
}

