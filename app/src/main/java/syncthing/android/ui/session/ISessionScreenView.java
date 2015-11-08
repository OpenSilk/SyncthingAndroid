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

import android.content.Context;

import java.util.List;

/**
 * Created by drew on 10/11/15.
 */
public interface ISessionScreenView {
    Context getContext();
    void initialize(List<NotifCard> notifs, List<FolderCard> folders, MyDeviceCard myDevice, List<DeviceCard> devices);
    void refreshNotifications(List<NotifCard> notifs);
    List<NotifCard> getNotifications();
    void refreshFolders(List<FolderCard> folders);
    List<FolderCard> getFolders();
    void refreshThisDevice(MyDeviceCard myDevice);
    MyDeviceCard getThisDevice();
    void refreshDevices(List<DeviceCard> devices);
    List<DeviceCard> getDevices();
    void showEmpty(boolean animate);
    void showList(boolean animate);
    void showLoading();
    void updateToolbarState(boolean show);
}
