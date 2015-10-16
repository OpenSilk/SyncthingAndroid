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
public class SessionRecyclerAdapterTest {

    @Spy SessionRecyclerAdapter adapter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNotificationAdd() {
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs, true);
        verify(adapter).notifyItemRangeInserted(0, 2);
    }

    @Test
    public void testNotificationReplaceMore() {
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs, false);
        List<NotifCard> notifs2 = new LinkedList<>();
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs2, true);
        verify(adapter).notifyItemRangeChanged(0, 2);
        verify(adapter).notifyItemRangeInserted(2, 2);
    }

    @Test
    public void testNotificationReplaceLess() {
        List<NotifCard> notifs2 = new LinkedList<>();
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        notifs2.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs2, false);
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs, true);
        verify(adapter).notifyItemRangeChanged(0, 3);
        verify(adapter).notifyItemRangeRemoved(3, 4);
    }

    @Test
    public void testFolderAdd() {
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs, false);
        List<FolderCard> folders = new LinkedList<>();
        folders.add(new FolderCard(null, null));
        folders.add(new FolderCard(null, null));
        folders.add(new FolderCard(null, null));
        adapter.setFolders(folders, true);
        verify(adapter).notifyItemRangeInserted(4, 3);
    }

    @Test
    public void testFolderReplaceMore() {
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs, false);
        List<FolderCard> folders = new LinkedList<>();

        FolderConfig f1 = new FolderConfig();
        f1.id = "id1";
        folders.add(new FolderCard(f1, null));

        FolderConfig f2 = new FolderConfig();
        f2.id = "id2";
        folders.add(new FolderCard(f2, null));

        adapter.setFolders(folders, false);
        List<FolderCard> folders2 = new LinkedList<>();

        FolderConfig f3 = new FolderConfig();
        f3.id = "id1";
        folders2.add(new FolderCard(f3, null));

        FolderConfig f4 = new FolderConfig();
        f4.id = "id2";
        folders2.add(new FolderCard(f4, null));

        FolderConfig f5 = new FolderConfig();
        f5.id = "id3";
        folders2.add(new FolderCard(f5, null));

        FolderConfig f6 = new FolderConfig();
        f6.id = "id4";
        folders2.add(new FolderCard(f6, null));

        adapter.setFolders(folders2, true);
        verify(adapter).notifyItemRangeChanged(4,2);
        verify(adapter).notifyItemRangeInserted(6,2);
    }

    @Test
    public void testFolderReplaceLess() {
        List<FolderCard> folders2 = new LinkedList<>();
        FolderConfig f3 = new FolderConfig();
        f3.id = "id1";
        folders2.add(new FolderCard(f3, null));

        FolderConfig f4 = new FolderConfig();
        f4.id = "id2";
        folders2.add(new FolderCard(f4, null));

        FolderConfig f5 = new FolderConfig();
        f5.id = "id3";
        folders2.add(new FolderCard(f5, null));

        FolderConfig f6 = new FolderConfig();
        f6.id = "id4";
        folders2.add(new FolderCard(f6, null));

        adapter.setFolders(folders2, false);
        List<FolderCard> folders = new LinkedList<>();

        FolderConfig f1 = new FolderConfig();
        f1.id = "id1";
        folders.add(new FolderCard(f1, null));

        FolderConfig f2 = new FolderConfig();
        f2.id = "id2";
        folders.add(new FolderCard(f2, null));

        adapter.setFolders(folders, true);
        verify(adapter).notifyItemRangeChanged(1,2);
        verify(adapter).notifyItemRangeRemoved(3, 2);
    }

    @Test
    public void testSetThisDevice() {
        List<FolderCard> folders = new LinkedList<>();
        folders.add(new FolderCard(null, null));
        folders.add(new FolderCard(null, null));
        adapter.setFolders(folders, false);
        adapter.setThisDevice(new MyDeviceCard(null, null, null, null), true);
        verify(adapter).notifyItemInserted(4);
        adapter.setThisDevice(new MyDeviceCard(null, null, null, null), true);
        verify(adapter).notifyItemChanged(4);
    }

    @Test
    public void testSetDevices() {
        List<FolderCard> folders = new LinkedList<>();
        folders.add(new FolderCard(null, null));
        folders.add(new FolderCard(null, null));
        adapter.setFolders(folders, false);
        List<DeviceCard> devices = new LinkedList<>();
        devices.add(new DeviceCard(null, null, null, 0));
        devices.add(new DeviceCard(null, null, null, 0));
        devices.add(new DeviceCard(null, null, null, 0));
        adapter.setDevices(devices, true);
        verify(adapter).notifyItemRangeInserted(4,3);
    }

    @Test
    public void testSetDevicesMore() {
        List<DeviceCard> devices = new LinkedList<>();
        DeviceConfig d1 = new DeviceConfig();
        d1.deviceID = "id1";
        devices.add(new DeviceCard(d1, null, null, 0));

        DeviceConfig d2 = new DeviceConfig();
        d2.deviceID = "id2";
        devices.add(new DeviceCard(d2, null, null, 0));

        DeviceConfig d3 = new DeviceConfig();
        d3.deviceID = "id3";
        devices.add(new DeviceCard(d3, null, null, 0));

        adapter.setDevices(devices, false);
        List<DeviceCard> devices2 = new LinkedList<>();

        DeviceConfig d4 = new DeviceConfig();
        d4.deviceID = "id1";
        devices2.add(new DeviceCard(d4, null, null, 0));

        DeviceConfig d5 = new DeviceConfig();
        d5.deviceID = "id2";
        devices2.add(new DeviceCard(d5, null, null, 0));

        DeviceConfig d6 = new DeviceConfig();
        d6.deviceID = "id3";
        devices2.add(new DeviceCard(d6, null, null, 0));

        DeviceConfig d7 = new DeviceConfig();
        d7.deviceID = "id4";
        devices2.add(new DeviceCard(d7, null, null, 0));

        DeviceConfig d8 = new DeviceConfig();
        d8.deviceID = "id5";
        devices2.add(new DeviceCard(d8, null, null, 0));

        adapter.setDevices(devices2, true);
        verify(adapter).notifyItemRangeChanged(2, 3);
        verify(adapter).notifyItemRangeInserted(5, 2);
    }

    @Test
    public void testSetDevicesLess() {
        List<DeviceCard> devices2 = new LinkedList<>();
        DeviceConfig d4 = new DeviceConfig();
        d4.deviceID = "id1";
        devices2.add(new DeviceCard(d4, null, null, 0));

        DeviceConfig d5 = new DeviceConfig();
        d5.deviceID = "id2";
        devices2.add(new DeviceCard(d5, null, null, 0));

        DeviceConfig d6 = new DeviceConfig();
        d6.deviceID = "id3";
        devices2.add(new DeviceCard(d6, null, null, 0));

        DeviceConfig d7 = new DeviceConfig();
        d7.deviceID = "id4";
        devices2.add(new DeviceCard(d7, null, null, 0));

        DeviceConfig d8 = new DeviceConfig();
        d8.deviceID = "id5";
        devices2.add(new DeviceCard(d8, null, null, 0));

        adapter.setDevices(devices2, false);
        List<DeviceCard> devices = new LinkedList<>();

        DeviceConfig d1 = new DeviceConfig();
        d1.deviceID = "id1";
        devices.add(new DeviceCard(d1, null, null, 0));

        DeviceConfig d2 = new DeviceConfig();
        d2.deviceID = "id2";
        devices.add(new DeviceCard(d2, null, null, 0));

        DeviceConfig d3 = new DeviceConfig();
        d3.deviceID = "id3";
        devices.add(new DeviceCard(d3, null, null, 0));

        adapter.setDevices(devices, true);
        verify(adapter).notifyItemRangeChanged(2, 3);
        verify(adapter).notifyItemRangeRemoved(5, 2);
    }

    @Test
    public void testFindFolderOffset() {
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs, false);
        int loc = adapter.findFolderOffset(0);
        assertEquals(loc, 3); //+1 for header
    }

    @Test
    public void testfindThisDevicePos() {
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs, false);
        List<FolderCard> folders = new LinkedList<>();
        folders.add(new FolderCard(null, null));
        folders.add(new FolderCard(null, null));
        folders.add(new FolderCard(null, null));
        adapter.setFolders(folders, false);
        int loc = adapter.findThisDevicePos();
        assertEquals(loc, 7); //+2 for headers
    }

    @Test
    public void testFindDeviceOffset() {
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);
        notifs.add(NotifCardRestart.INSTANCE);
        adapter.setNotifications(notifs, false);
        List<FolderCard> folders = new LinkedList<>();
        folders.add(new FolderCard(null, null));
        folders.add(new FolderCard(null, null));
        folders.add(new FolderCard(null, null));
        adapter.setFolders(folders, false);
        int loc = adapter.findDeviceOffset(0);
        assertEquals(loc, 7); //+2 for headers
    }

    @Test
    public void testgetItem() {
        List<NotifCard> notifs = new LinkedList<>();
        notifs.add(NotifCardRestart.INSTANCE);//0
        notifs.add(NotifCardRestart.INSTANCE);//1
        adapter.setNotifications(notifs, false);
        //2
        List<FolderCard> folders = new LinkedList<>();
        folders.add(new FolderCard(null, null));//3
        folders.add(new FolderCard(null, null));//4
        folders.add(new FolderCard(null, null));//5
        adapter.setFolders(folders, false);
        //6
        MyDeviceCard myDevice = new MyDeviceCard(null, null, null, null);
        adapter.setThisDevice(myDevice, true);//7
        List<DeviceCard> devices = new LinkedList<>();
        devices.add(new DeviceCard(null, null, null, 0));//8
        devices.add(new DeviceCard(null, null, null, 0));//9
        devices.add(new DeviceCard(null, null, null, 0));//10
        adapter.setDevices(devices, false);
        assertTrue((notifs.get(0) == adapter.getItem(0)));
        assertTrue((folders.get(1) == adapter.getItem(4)));
        assertTrue((myDevice == adapter.getItem(7)));
        assertTrue((devices.get(1) == adapter.getItem(9)));
        assertEquals(11, adapter.getItemCount());
    }
}
