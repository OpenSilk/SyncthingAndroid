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

package syncthing.android.ui.session;

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
import syncthing.android.ui.common.ExpandableCard;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by drew on 3/14/15.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AdapterIdsTest {

    @Test
    public void testAdapterIds () {
        DeviceConfig d1 = new DeviceConfig();
        d1.deviceID = "123456";
        DeviceCard dc1 = new DeviceCard(d1, null, null, 0);
        DeviceConfig d2 = new DeviceConfig();
        d2.deviceID = "567890";
        DeviceCard dc2 = new DeviceCard(d2, null, null, 0);

        assertNotEquals(dc1.adapterId(), dc2.adapterId());

        FolderConfig f1 = new FolderConfig();
        f1.id = "123456";
        FolderCard fc1 = new FolderCard(f1, null);
        FolderConfig f2 = new FolderConfig();
        f2.id = "567890";
        FolderCard fc2 = new FolderCard(f2, null);

        assertNotEquals(fc1.adapterId(), fc2.adapterId());

        HeaderCard hc1 = HeaderCard.FOLDER;
        HeaderCard hc2 = HeaderCard.DEVICE;

        assertNotEquals(hc1.adapterId(), hc2.adapterId());

        DeviceConfig d3 = new DeviceConfig();
        d3.deviceID = "123456";
        MyDeviceCard mdc1 = new MyDeviceCard(d3, null, null, null);
        DeviceConfig d4 = new DeviceConfig();
        d4.deviceID = "567890";
        MyDeviceCard mdc2 = new MyDeviceCard(d4, null, null, null);

        assertNotEquals(mdc1.adapterId(), mdc2.adapterId());

        NotifCardError nce1 = new NotifCardError(null);

        NotifCardRejFolder ncrf1 = new NotifCardRejFolder("default-123456", null);
        NotifCardRejFolder ncrf2 = new NotifCardRejFolder("default-567890", null);

        assertNotEquals(ncrf1.adapterId(), ncrf2.adapterId());

        NotifCardRejDevice ncrd1 = new NotifCardRejDevice("123456", null);
        NotifCardRejDevice ncrd2 = new NotifCardRejDevice("567890", null);

        assertNotEquals(ncrd1.adapterId(), ncrd2.adapterId());

        NotifCardRestart ncr = NotifCardRestart.INSTANCE;

        assertNotEquals(dc1.adapterId(), fc1.adapterId());
        assertNotEquals(dc1.adapterId(), hc1.adapterId());
        assertNotEquals(dc1.adapterId(), mdc1.adapterId());
        assertNotEquals(dc1.adapterId(), nce1.adapterId());
        assertNotEquals(dc1.adapterId(), ncrf1.adapterId());
        assertNotEquals(dc1.adapterId(), ncrd1.adapterId());
        assertNotEquals(dc1.adapterId(), ncr.adapterId());
        //Good enough i guess;
    }
}
