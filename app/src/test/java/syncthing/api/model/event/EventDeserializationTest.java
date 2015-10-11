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

package syncthing.api.model.event;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import syncthing.api.GsonModule;
import syncthing.api.model.ModelState;

import static org.assertj.core.api.Assertions.*;

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
