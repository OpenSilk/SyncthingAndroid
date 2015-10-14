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

import syncthing.android.ui.common.ExpandableCard;

/**
 * Created by drew on 10/11/15.
 */
public interface ISessionScreenView {
    Context getContext();
    void initialize(List<NotifCard> notifs, List<FolderCard> folders, MyDeviceCard myDevice, List<DeviceCard> devices);
    void refreshNotifications(List<NotifCard> notifs);
    void refreshFolders(List<FolderCard> folders);
    void refreshThisDevice(MyDeviceCard myDevice);
    void refreshDevices(List<DeviceCard> devices);
    void showSavingDialog();
    void dismissSavingDialog();
    void showRestartDialog();
    void dismissRestartDialog();
    void showProgressDialog(String msg);
    void dismissProgressDialog();
    void showErrorDialog(String title, String msg);
    void dismissErrorDialog();
    void showConfigSaved();
    void setListEmpty(boolean show, boolean animate);
    void setListShown(boolean show, boolean animate);
    void setLoading(boolean loading);
}
