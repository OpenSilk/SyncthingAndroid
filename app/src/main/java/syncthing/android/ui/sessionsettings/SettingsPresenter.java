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
import android.view.View;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;

import javax.inject.Inject;

import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.settings.AppSettings;
import syncthing.android.model.Credentials;
import syncthing.api.SessionManager;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;

/**
 * Created by drew on 3/17/15.
 */
@ScreenScope
public class SettingsPresenter extends EditPresenter<SettingsScreenView> {

    final AppSettings appSettings;
    final String hiddenPass = SyncthingUtils.hiddenString(20);

    DeviceConfig thisDevice;
    OptionsConfig options;
    GUIConfig guiConfig;

    @Inject
    public SettingsPresenter(
            SessionManager manager,
            DialogPresenter dialogPresenter,
            ActivityResultsController activityResultContoller,
            EditPresenterConfig config,
            AppSettings appSettings
    ) {
        super(manager, dialogPresenter, activityResultContoller, config);
        this.appSettings = appSettings;
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

    public void copyApiKey(View btn) {
        if (!hasView()) return;
        SettingsScreenView v = getView();
        SyncthingUtils.copyToClipboard(v.getContext(),
                v.getContext().getString(R.string.api_key),
                v.binding.editApikey.getText().toString());
    }

    public void regenApiKey(View btn) {
        if (!hasView()) return;
        SettingsScreenView v = getView();
        String key = SyncthingUtils.randomString(32);
        v.binding.editApikey.setText(key);
    }

    public void saveConfig(View btn) {
        if (!hasView()) return;
        SettingsScreenView v = getView();
        thisDevice.name = v.binding.editDeviceName.getText().toString();
        if (!validateListenAddresses(v.binding.editProtocolListenAddr.getText().toString())) {
            return;
        }
        options.listenAddress = SyncthingUtils.rollArray(v.binding.editProtocolListenAddr.getText().toString());
        if (!validateMaxRecv(v.binding.editIncomingRateLimit.getText().toString())) {
            return;
        }
        options.maxRecvKbps = Integer.valueOf(v.binding.editIncomingRateLimit.getText().toString());
        if (!validateMaxSend(v.binding.editOutgoingRateLimit.getText().toString())) {
            return;
        }
        options.maxSendKbps = Integer.valueOf(v.binding.editOutgoingRateLimit.getText().toString());
        options.upnpEnabled = v.binding.checkEnableUpnp.isChecked();
        options.globalAnnounceEnabled = v.binding.checkGlobalDiscovery.isChecked();
        options.localAnnounceEnabled = v.binding.checkLocalDiscovery.isChecked();
        if (!validateGlobalDiscoveryServers(v.binding.editGlobalDiscoveryServer.getText().toString())) {
            return;
        }
        options.globalAnnounceServers = SyncthingUtils.rollArray(v.binding.editGlobalDiscoveryServer.getText().toString());
        guiConfig.address = v.binding.editGuiListenAddr.getText().toString();
        guiConfig.user = v.binding.editGuiUser.getText().toString();
        if (!StringUtils.equals(v.binding.editGuiPass.getText().toString(), hiddenPass)) {
            guiConfig.password = v.binding.editGuiPass.getText().toString();
        }
        guiConfig.useTLS = v.binding.checkUseHttps.isChecked();
        options.startBrowser = v.binding.checkStartBrowser.isChecked();
        options.urAccepted = (v.binding.checkUsageReporting.isChecked()) ? 1000 : -1;
        guiConfig.apiKey = v.binding.editApikey.getText().toString();

        unsubscribe(saveSubscription);
        onSaveStart();
        final String deviceName = thisDevice.name;
        saveSubscription = controller.editSettings(thisDevice, options, guiConfig,
                this::onSavefailed,
                () -> {
                    maybeUpdateAlias(deviceName);
                    onSaveSuccessfull();
                }
        );
    }

    void maybeUpdateAlias(String deviceName) {
        if (!StringUtils.isEmpty(deviceName)
                && !StringUtils.equalsIgnoreCase(credentials.alias, deviceName)) {
            appSettings.saveCredentials(new Credentials(deviceName, credentials.id,
                    credentials.url, credentials.apiKey, credentials.caCert));
        }
    }

}
