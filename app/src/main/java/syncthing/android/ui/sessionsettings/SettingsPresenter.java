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

package syncthing.android.ui.sessionsettings;

import android.os.Bundle;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;

import javax.inject.Inject;

import syncthing.api.SessionManager;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;

/**
 * Created by drew on 3/17/15.
 */
@ScreenScope
public class SettingsPresenter extends EditPresenter<SettingsScreenView> {

    DeviceConfig thisDevice;
    OptionsConfig options;
    GUIConfig guiConfig;

    @Inject
    public SettingsPresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            EditPresenterConfig config
    ) {
        super(manager, dialogPresenter, activityResultContoller, config);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (savedInstanceState == null) {
            thisDevice = SerializationUtils.clone(controller.getThisDevice());
            options = SerializationUtils.clone(controller.getConfig().options);
            guiConfig = SerializationUtils.clone(controller.getConfig().gui);
        } else {
            thisDevice = (DeviceConfig) savedInstanceState.getSerializable("device");
            options = (OptionsConfig) savedInstanceState.getSerializable("options");
            guiConfig = (GUIConfig) savedInstanceState.getSerializable("guiconfig");
        }
        getView().initialize(thisDevice, options, guiConfig, savedInstanceState != null);
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        //TODO parcelable
        outState.putSerializable("device", thisDevice);
        outState.putSerializable("options", options);
        outState.putSerializable("guiconfig", guiConfig);
    }


    boolean validateListenAddresses(CharSequence text) {
        //TODO
        return true;
    }

    boolean validateMaxSend(CharSequence text) {
        if (!StringUtils.isNumeric(text) || Integer.decode(text.toString()) >= 0) {
            //TODO
        }
        return true;
    }

    boolean validateMaxRecv(CharSequence text) {
        if (!StringUtils.isNumeric(text) || Integer.decode(text.toString()) >= 0) {
            //TODO
        }
        return true;
    }

    boolean validateGlobalDiscoveryServers(CharSequence text) {
        //TODO
        return true;
    }

    void saveConfig(DeviceConfig device, OptionsConfig options, GUIConfig guiConfig) {
        if (saveSubscription != null) {
            saveSubscription.unsubscribe();
        }
        onSaveStart();
        saveSubscription = controller.editSettings(device, options, guiConfig,
                this::onSavefailed,
                this::onSaveSuccessfull
        );
    }

}
