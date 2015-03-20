package syncthing.android.service;

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
    public void testRandomString() {
        assertEquals(23, randomString(23).length());
    }

}