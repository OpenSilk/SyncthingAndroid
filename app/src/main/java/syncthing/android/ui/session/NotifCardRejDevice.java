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

import java.util.Collections;

import syncthing.android.R;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.event.DeviceRejected;

/**
 * Created by drew on 3/6/15.
 */
public class NotifCardRejDevice extends NotifCardRej<DeviceRejected> {

    public NotifCardRejDevice(SessionPresenter presenter, String key, DeviceRejected event) {
        super(presenter, Kind.DEVICE_REJ, key, event);
    }

    @Override
    public int getLayout() {
        return R.layout.session_notif_device_rej;
    }

    @Bindable
    public String getTime() {
        return event.time.toString("H:mm:ss");
    }

    @Bindable
    public String getDeviceID() {
        return event.data.device;
    }

    @Bindable
    public String getAddress() {
        return event.data.address;
    }

    public void addDevice(View btn) {
        presenter.showSavingDialog();
        //TODO stop doing this (move logic somewhere else)
        DeviceConfig deviceConfig = new DeviceConfig();
        deviceConfig.deviceID = getDeviceID();
        presenter.controller.editDevice(deviceConfig, Collections.emptyMap(),
                t -> {
                    presenter.showError("Save failed", t.getMessage());
                },
                () -> {
                    presenter.dismissSavingDialog();
                    presenter.showSuccessMsg();
                    dismissDevice(null);
                }
        );
    }

    public void ignoreDevice(View btn) {
        presenter.showSavingDialog();
        //TODO stop doing this (move logic somewhere else)
        presenter.controller.ignoreDevice(getDeviceID(),
                t -> {
                    presenter.showError("Ignore failed", t.getMessage());
                },
                () -> {
                    presenter.dismissSavingDialog();
                    presenter.showSuccessMsg();
                    dismissDevice(null);
                }
        );
    }

    public void dismissDevice(View btn) {
        presenter.controller.removeDeviceRejection(key);
    }
}
