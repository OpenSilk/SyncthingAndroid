package syncthing.android.service;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;
import java.util.List;

import syncthing.android.ui.common.Card;
import syncthing.api.model.DeviceConfig;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static syncthing.android.service.SyncthingUtils.*;

/**
 * Created by drew on 3/14/15.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class SyncthingUtilsTest {

    @Test
    public void testGetDisplayName() {
        DeviceConfig d = new DeviceConfig();
        d.name = "Name";
        assertEquals("Name", getDisplayName(d));
        d.name = "";
        d.deviceID = "07adad5c-a57d-4693-9b8b-6e4e2593c4e0";
        assertEquals("07ADAD", getDisplayName(d));
        d.name = null;
        assertEquals("07ADAD", getDisplayName(d));
        d.deviceID = null;
        assertEquals("[unknown]", getDisplayName(d));
        d.deviceID = "123456";
        assertEquals("123456", getDisplayName(d));
    }

    @Test
    public void testfileSize() {
        assertEquals("1.5 MiB", humanReadableSize(1572864));
    }

    @Test
    public void testTransferrate() {
        assertEquals("1.5 MiB/s", humanReadableTransferRate(1572864));
    }

    @Test
    public void testRollArray() {
        assertArrayEquals(new String[] {"one", "two", "three"}, rollArray("one,two,three"));
        assertArrayEquals(new String[] {"one", "two", "three"}, rollArray("one two three"));
        assertArrayEquals(new String[] {"one", "two", "three"}, rollArray("one, two,  three"));
    }

    @Test
    public void testUnrollArray() {
        assertEquals("one,two,three", unrollArray(new String[] {"one", "two", "three"}));
    }


    @Test
    public void testisNowBetweenRange() {
        long now = DateTime.parse("2015-03-21T14:12:00").getMillis();//2:12pm
        DateTimeUtils.setCurrentMillisFixed(now);
        // same time is true
        long start = parseTime("00:00");
        long end = parseTime("00:00");
        assertEquals(isNowBetweenRange(start, end), true);
        // start < end
        start = parseTime("12:00");//noon
        end = parseTime("18:00");//6pm
        // 12pm < 2pm < 6pm so true
        assertEquals(isNowBetweenRange(start, end), true);
        // start > end
        start = parseTime("23:00");//11pm;
        end = parseTime("06:00");//6am;
        // 2pm > 6am so false
        assertEquals(isNowBetweenRange(start, end), false);
        now = DateTime.parse("2015-03-21T03:12:00").getMillis();//3:12am
        DateTimeUtils.setCurrentMillisFixed(now);
        // 3am > 11pm so true
        assertEquals(isNowBetweenRange(start, end), true);
        now = DateTime.parse("2015-03-21T08:12:00").getMillis();//8:12am
        DateTimeUtils.setCurrentMillisFixed(now);
        // 8am > 6am so false
        assertEquals(isNowBetweenRange(start, end), false);
    }

    @Test
    public void testgetNextIntervalforRange() {
        DateTime now = DateTime.parse("2015-03-21T14:12:00"); //2:12pm

        // start < end uses same day

        // can only run between 3 and 4 pm

        long start = parseTime("15:12"); //3:12pm
        long end = parseTime("16:12");//4:12pm
        Interval interval = getIntervalForRange(now, start, end);
        assertEquals(interval.getStartMillis(), DateTime.parse("2015-03-21T15:12:00").getMillis());
        assertEquals(interval.getEndMillis(), DateTime.parse("2015-03-21T16:12:00").getMillis());

        //inside interval with start > end rolls end to next day

        //cannot run between 12 and 1 pm

        start = parseTime("13:12"); //1:12pm
        end = parseTime("12:12");//12:12pm
        interval = getIntervalForRange(now, start, end);
        assertEquals(interval.getStartMillis(), DateTime.parse("2015-03-21T13:12:00").getMillis());
        assertEquals(interval.getEndMillis(), DateTime.parse("2015-03-22T12:12:00").getMillis());

        //outside interval with start > end rolls start to previous day

        //cannot run between 10am and 3pm

        start = parseTime("15:12");//3:12pm
        end = parseTime("10:12"); //10:12am
        interval = getIntervalForRange(now, start, end);
        assertEquals(interval.getStartMillis(), DateTime.parse("2015-03-20T15:12:00").getMillis());
        assertEquals(interval.getEndMillis(), DateTime.parse("2015-03-21T10:12:00").getMillis());

    }
}