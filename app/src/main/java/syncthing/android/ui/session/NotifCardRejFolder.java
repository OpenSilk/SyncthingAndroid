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

import android.databinding.Bindable;
import android.view.View;

import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.event.FolderRejected;

/**
 * Created by drew on 3/6/15.
 */
public class NotifCardRejFolder extends NotifCardRej<FolderRejected> {

    public NotifCardRejFolder(SessionPresenter presenter, String key, FolderRejected event) {
        super(presenter, Kind.FOLDER_REJ, key, event);
    }

    @Override
    public int getLayout() {
        return R.layout.session_notif_folder_rej;
    }

    @Bindable
    public String getTime() {
        return event.time.toString("H:mm:ss");
    }

    @Bindable
    public boolean isShare() {
        return presenter.controller.getFolder(event.data.folder) != null;
    }

    @Bindable
    public String getDeviceName() {
        DeviceConfig device = presenter.controller.getDevice(event.data.device);
        if (device == null) {
            device = new DeviceConfig();
            device.deviceID = event.data.device;
        }
        return SyncthingUtils.getDisplayName(device);
    }

    @Bindable
    public String getFolderName() {
        return event.data.folder;
    }

    public void addFolder(View btn) {
        if (isShare()) {
            presenter.showSavingDialog();
            //TODO stop doing this (move logic somewhere else)
            presenter.controller.shareFolder(event.data.folder, event.data.device,
                    t -> {
                        presenter.showError("Share failed", t.getMessage());
                    },
                    () -> {
                        presenter.dismissSavingDialog();
                        presenter.showSuccessMsg();
                        dismissFolder(null);
                    }
            );
        } else {
            dismissFolder(null);
            presenter.openEditFolderScreen(event.data.folder, event.data.device);
        }
    }

    public void dismissFolder(View btn) {
        presenter.controller.removeFolderRejection(key);
    }

}
