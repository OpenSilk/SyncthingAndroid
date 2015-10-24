/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package syncthing.api.model.event;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import syncthing.api.GsonModule;
import syncthing.api.model.ModelState;
import syncthing.api.model.event.Event;
import syncthing.api.model.event.EventType;
import syncthing.api.model.event.FolderSummary;
import syncthing.api.model.event.StateChanged;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by drew on 10/11/15.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class EventDeserializationTest {

    Gson mGson;

    @Before
    public void setup() {
        mGson = new GsonModule().provideGson();
    }

    @Test
    public void test1() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("events1.json");
        Reader reader = new InputStreamReader(is, "UTF-8");
        Event[] events = mGson.fromJson(reader, Event[].class);
        is.close();
        assertThat(events).isNotNull();
        assertThat(events.length).isEqualTo(5);
        assertThat(events[0].type).isEqualTo(EventType.PING);
        assertThat(events[1].type).isEqualTo(EventType.DEVICE_DISCONNECTED);
        assertThat(events[2].type).isEqualTo(EventType.STATE_CHANGED);
        assertThat(((StateChanged.Data) events[2].data).from).isEqualTo(ModelState.IDLE);
        assertThat(events[3].type).isEqualTo(EventType.STATE_CHANGED);
        assertThat(((StateChanged.Data) events[3].data).from).isEqualTo(ModelState.SYNCING);
        assertThat(events[4].type).isEqualTo(EventType.FOLDER_SUMMARY);
        assertThat(((FolderSummary.Data) events[4].data).summary.inSyncBytes).isEqualTo(927210);
    }

    //TODO test all events

}
