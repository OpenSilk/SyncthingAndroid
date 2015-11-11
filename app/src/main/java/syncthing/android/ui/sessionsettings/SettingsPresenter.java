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

import android.content.Context;
import android.databinding.Bindable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import javax.inject.Inject;

import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.settings.AppSettings;
import syncthing.api.Credentials;
import syncthing.api.SessionManager;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;
import timber.log.Timber;

/**
 * Created by drew on 3/17/15.
 */
@ScreenScope
public class SettingsPresenter extends EditPresenter<SettingsScreenView> {

    final Context appContext;
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
            ToolbarOwner toolbarOwner,
            EditPresenterConfig config,
            @ForApplication Context appContext,
            AppSettings appSettings
    ) {
        super(manager, dialogPresenter, activityResultContoller, toolbarOwner, config);
        this.appContext = appContext;
        this.appSettings = appSettings;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!wasPreviouslyLoaded && savedInstanceState != null) {
            thisDevice = (DeviceConfig) savedInstanceState.getSerializable("device");
            options = (OptionsConfig) savedInstanceState.getSerializable("options");
            guiConfig = (GUIConfig) savedInstanceState.getSerializable("guiconfig");
        } else if (!wasPreviouslyLoaded) {
            DeviceConfig d = controller.getThisDevice();
            if (d != null) {
                thisDevice = d.clone();
            }
            OptionsConfig o = controller.getConfig().options;
            if (o != null) {
                options = o.clone();
            }
            GUIConfig g = controller.getConfig().gui;
            if (g != null) {
                guiConfig = g.clone();
            }
        }
        wasPreviouslyLoaded = true;
        if (thisDevice == null || options == null || guiConfig == null) {
            Timber.e("Incomplete data! Cannot continue");
            dismissDialog();
        }
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
        if (StringUtils.isEmpty(text)) {
            //TODO
            return false;
        } else if (!StringUtils.startsWith(text, "tcp://")) {
            //TODO
            return false;
        }
        return true;
    }

    boolean validateMaxSend(CharSequence text) {
        if (!StringUtils.isNumeric(text) || Integer.decode(text.toString()) >= 0) {
            //TODO
            return false;
        }
        return true;
    }

    boolean validateMaxRecv(CharSequence text) {
        if (!StringUtils.isNumeric(text) || Integer.decode(text.toString()) >= 0) {
            //TODO
            return false;
        }
        return true;
    }

    boolean validateGlobalDiscoveryServers(CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            //TODO
            return false;
        }
        return true;
    }

    boolean validateGuiListenAddress(CharSequence text) {
        if (!StringUtils.isEmpty(text)) {
            //TODO
            return false;
        }
        return true;
    }

    public void showApiKeyOverflow(final View btn) {
        PopupMenu m = new PopupMenu(btn.getContext(), btn);
        m.inflate(R.menu.apikey_overflow);
        m.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.copy:
                        copyApiKey(btn);
                        return true;
                    case R.id.generate:
                        regenApiKey(btn);
                        return true;
                    default:
                        return false;
                }
            }
        });
        m.show();
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
        setApiKey(key);
        notifyChange(syncthing.android.BR.apiKey);
    }

    public void saveConfig(View btn) {
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

    @Bindable
    public String getDeviceName() {
        return SyncthingUtils.getDisplayName(thisDevice);
    }

    public void setDeviceName(CharSequence deviceName) {
        thisDevice.name = StringUtils.isEmpty(deviceName) ? "" : deviceName.toString();
    }

    @Bindable
    public String getListenAddressText() {
        return SyncthingUtils.unrollArray(options.listenAddress);
    }

    public void setListenAddress(CharSequence text) {
        if (validateListenAddresses(text)) {
            options.listenAddress = SyncthingUtils.rollArray(text.toString());
        }
    }

    @Bindable
    public String getMaxRecvKbps() {
        return String.valueOf(options.maxRecvKbps);
    }

    public void setMaxRecvKpbs(CharSequence text) {
        if (validateMaxRecv(text)) {
            options.maxRecvKbps = Integer.valueOf(text.toString());
        }
    }

    @Bindable
    public String getMaxSendKbps() {
        return String.valueOf(options.maxSendKbps);
    }

    public void setMaxSendKbps(CharSequence text) {
        if (validateMaxSend(text)) {
            options.maxSendKbps = Integer.valueOf(text.toString());
        }
    }

    @Bindable
    public boolean isUpnpEnabled() {
        return options.upnpEnabled;
    }

    public void setUpnpEnabled(boolean enabled) {
        options.upnpEnabled = enabled;
    }

    @Bindable
    public boolean isLocalAnnounceEnabled() {
        return options.localAnnounceEnabled;
    }

    public void setLocalAnnounceEnabled(boolean enabled) {
        options.localAnnounceEnabled = enabled;
    }

    @Bindable
    public boolean isGlobalAnnounceEnabled() {
        return options.globalAnnounceEnabled;
    }

    public void setGlobalAnnounceEnabled(boolean enabled) {
        options.globalAnnounceEnabled = enabled;
        notifyChange(syncthing.android.BR.globalAnnounceEnabled);
    }

    @Bindable
    public String getGlobalAnnounceServersText() {
        return SyncthingUtils.unrollArray(options.globalAnnounceServers);
    }

    public void setGlobalAnnounceServers(CharSequence text) {
        if (validateGlobalDiscoveryServers(text)) {
            options.globalAnnounceServers = SyncthingUtils.rollArray(text.toString());
        }
    }

    @Bindable
    public String getGuiListenAddress() {
        return guiConfig.address;
    }

    public void setGuiListenAddress(CharSequence text) {
        if (validateGuiListenAddress(text)) {
            guiConfig.address = text.toString();
        }
    }

    @Bindable
    public String getGuiUser() {
        return guiConfig.user;
    }

    public void setGuiUser(CharSequence text) {
        guiConfig.user = StringUtils.isEmpty(text) ? "" : text.toString();
    }

    @Bindable
    public String getGuiPassword() {
        return hiddenPass;
    }

    public void setGuiPassword(CharSequence text) {
        if (!StringUtils.equals(hiddenPass, text)) {
            guiConfig.password = StringUtils.isEmpty(text) ? "" : text.toString();
        }
    }

    @Bindable
    public boolean isUseTLS() {
        return guiConfig.useTLS;
    }

    public void setUseTLS(boolean enable) {
        guiConfig.useTLS = enable;
    }

    @Bindable
    public boolean isStartBrowser() {
        return options.startBrowser;
    }

    public void setStartBrowser(boolean enable) {
        options.startBrowser = enable;
    }

    @Bindable
    public boolean isURAccepted() {
        return options.urAccepted > 0;
    }

    public void setURAccepted(boolean enable) {
        options.urAccepted = enable ? 1 : -1;
    }

    @Bindable
    public String getApiKey() {
        return guiConfig.apiKey;
    }

    public void setApiKey(String text) {
        guiConfig.apiKey = text;
    }

    @Bindable
    public boolean isHasClipboard() {
        return !SyncthingUtils.isClipBoardSupported(appContext);
    }

}
