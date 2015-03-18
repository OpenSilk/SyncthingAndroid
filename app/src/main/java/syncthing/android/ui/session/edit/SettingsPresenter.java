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

package syncthing.android.ui.session.edit;

import android.os.Bundle;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.Subscription;
import syncthing.api.SessionController;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;

/**
 * Created by drew on 3/17/15.
 */
@EditScope
public class SettingsPresenter extends ViewPresenter<SettingsScreenView> {

    final SessionController controller;
    final EditFragmentPresenter editFragmentPresenter;

    DeviceConfig thisDevice;
    OptionsConfig options;
    GUIConfig guiConfig;

    Subscription saveSubscription;

    @Inject
    public SettingsPresenter(
            SessionController controller,
            EditFragmentPresenter editFragmentPresenter
    ) {
        this.controller = controller;
        this.editFragmentPresenter = editFragmentPresenter;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        if (saveSubscription != null) {
            saveSubscription.unsubscribe();
        }
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

    void dismissDialog() {
        editFragmentPresenter.dismissDialog();
    }

    void saveConfig(DeviceConfig device, OptionsConfig options, GUIConfig guiConfig) {
        if (saveSubscription != null) {
            saveSubscription.unsubscribe();
        }
        saveSubscription = controller.editSettings(device, options, guiConfig,
                t -> {
                    if (hasView()) {
                        getView().showError(t.getMessage());
                    }
                },
                () -> {
                    if (hasView()) {
                        getView().showConfigSaved();
                    }
                    dismissDialog();
                });
    }

}
