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

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensilk.common.core.dagger2.ForApplication;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Named;

import static syncthing.android.service.ServiceSettings.*;
import static org.assertj.core.api.Assertions.*;
import static syncthing.android.service.SyncthingUtils.parseTime;

/**
 * Created by drew on 10/15/15.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ServiceSettingsTest {

    @Test
    public void testGetNextStartTimeFor() {
        long now = DateTime.parse("2015-03-21T14:12:00").getMillis();//2:12pm
        DateTimeUtils.setCurrentMillisFixed(now);

        //before interval is interval start when start < end

        long start = parseTime("15:12"); //3:12pm
        long end = parseTime("16:12");//4:12pm

        assertThat(getNextNextStartTimeFor(start, end)).isEqualTo(DateTime.parse("2015-03-21T15:12:00").getMillis());

        //inside interval rolls next day when start < end

        start = parseTime("13:12"); //1:12pm
        end = parseTime("16:12");//4:12pm

        assertThat(getNextNextStartTimeFor(start, end)).isEqualTo(DateTime.parse("2015-03-22T13:12:00").getMillis());

        //after interval rolls next day when start < end

        start = parseTime("10:12"); //10:12am
        end = parseTime("13:12");//1:12pm

        assertThat(getNextNextStartTimeFor(start, end)).isEqualTo(DateTime.parse("2015-03-22T10:12:00").getMillis());

        //inside interval rolls next day when start > end

        start = parseTime("13:12"); //1:12pm
        end = parseTime("12:12");//12:12pm

        assertThat(getNextNextStartTimeFor(start, end)).isEqualTo(DateTime.parse("2015-03-22T13:12:00").getMillis());

        //outside interval is same day when start > end

        start = parseTime("15:12"); //3:12pm
        end = parseTime("10:12");//10:12am

        assertThat(getNextNextStartTimeFor(start, end)).isEqualTo(DateTime.parse("2015-03-21T15:12:00").getMillis());
    }
}
