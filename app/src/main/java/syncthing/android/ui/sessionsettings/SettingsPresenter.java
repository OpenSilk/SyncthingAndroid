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
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;
import org.opensilk.common.ui.mortar.DialogPresenter;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import javax.inject.Inject;

import rx.functions.Action1;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.settings.AppSettings;
import syncthing.android.ui.binding.Action1IgnoreFirst;
import syncthing.api.Credentials;
import syncthing.api.SessionManager;
import syncthing.api.model.Config;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;
import timber.log.Timber;

/**
 * Created by drew on 3/17/15.
 */
@ScreenScope
public class SettingsPresenter extends EditPresenter<CoordinatorLayout> {

    final Context appContext;
    final AppSettings appSettings;

    DeviceConfig thisDevice;
    OptionsConfig options;
    GUIConfig guiConfig;

    String hiddenPass;

    String errorListenAddress;
    String errorGlobalDiscoverServers;
    String errorGuiListenAddress;

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
            Config config = controller.getConfig();
            if (config != null) {
                OptionsConfig o = config.options;
                if (o != null) {
                    options = o.clone();
                }
                GUIConfig g = config.gui;
                if (g != null) {
                    guiConfig = g.clone();
                }
            }
        }
        wasPreviouslyLoaded = true;
        if (thisDevice == null || options == null || guiConfig == null) {
            Timber.e("Incomplete data! Cannot continue");
            dismissDialog();
        }
        hiddenPass = getView().getContext().getString(R.string.ten_stars);
    }

    @Override
    protected void onSave(Bundle outState) {
        super.onSave(outState);
        //TODO parcelable
        outState.putSerializable("device", thisDevice);
        outState.putSerializable("options", options);
        outState.putSerializable("guiconfig", guiConfig);
    }

    @Bindable
    public String getDeviceName() {
        return SyncthingUtils.getDisplayName(thisDevice);
    }

    public void setDeviceName(CharSequence deviceName) {
        thisDevice.name = StringUtils.isEmpty(deviceName) ? "" : deviceName.toString();
    }

    public final Action1<CharSequence> actionSetDeviceName = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setDeviceName(charSequence);
        }
    };

    @Bindable
    public String getListenAddressText() {
        return SyncthingUtils.unrollArray(options.listenAddress);
    }

    public void setListenAddress(CharSequence text) {
        if (validateListenAddresses(text)) {
            options.listenAddress = SyncthingUtils.rollArray(text.toString());
        }
    }

    public final Action1<CharSequence> actionSetListenAddress = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setListenAddress(charSequence);
        }
    };

    @Bindable
    public String getListenAddressError() {
        return errorListenAddress;
    }

    public void setListenAddressError(String error) {
        if (!StringUtils.equals(errorListenAddress, error)) {
            errorListenAddress = error;
            notifyChange(syncthing.android.BR.listenAddressError);
        }
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

    @Bindable
    public String getMaxRecvKbps() {
        return String.valueOf(options.maxRecvKbps);
    }

    public void setMaxRecvKpbs(CharSequence text) {
        //input disallows non numeric text
        options.maxRecvKbps = StringUtils.isEmpty(text) ? 0 : Integer.valueOf(text.toString());
    }

    public final Action1<CharSequence> actionSetMaxRecvKbps = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setMaxRecvKpbs(charSequence);
        }
    };

    @Bindable
    public String getMaxSendKbps() {
        return String.valueOf(options.maxSendKbps);
    }

    public void setMaxSendKbps(CharSequence text) {
        //input disallows non numeric text
        options.maxSendKbps = StringUtils.isEmpty(text) ? 0 : Integer.valueOf(text.toString());
    }

    public final Action1<CharSequence> actionSetMaxSendKbps = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setMaxSendKbps(charSequence);
        }
    };

    @Bindable
    public boolean isUpnpEnabled() {
        return options.upnpEnabled;
    }

    public void setUpnpEnabled(boolean enabled) {
        options.upnpEnabled = enabled;
    }

    public final Action1<Boolean> actionSetUpnpEnabled = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            setUpnpEnabled(aBoolean);
        }
    };

    @Bindable
    public boolean isLocalAnnounceEnabled() {
        return options.localAnnounceEnabled;
    }

    public void setLocalAnnounceEnabled(boolean enabled) {
        options.localAnnounceEnabled = enabled;
    }

    public final Action1<Boolean> actionSetLocalAnnounceEnabled = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            setLocalAnnounceEnabled(aBoolean);
        }
    };

    @Bindable
    public boolean isGlobalAnnounceEnabled() {
        return options.globalAnnounceEnabled;
    }

    public void setGlobalAnnounceEnabled(boolean enabled) {
        options.globalAnnounceEnabled = enabled;
        notifyChange(syncthing.android.BR.globalAnnounceEnabled);
    }

    public final Action1<Boolean> actionSetGlobalAnnounceEnabled = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            setGlobalAnnounceEnabled(aBoolean);
        }
    };

    @Bindable
    public String getGlobalAnnounceServersText() {
        return SyncthingUtils.unrollArray(options.globalAnnounceServers);
    }

    public void setGlobalAnnounceServers(CharSequence text) {
        if (validateGlobalDiscoveryServers(text)) {
            options.globalAnnounceServers = SyncthingUtils.rollArray(text.toString());
        }
    }

    public final Action1<CharSequence> actionSetGlobalAnnounceServers = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setGlobalAnnounceServers(charSequence);
        }
    };

    @Bindable
    public String getGlobalAnnounceServersError() {
        return errorGlobalDiscoverServers;
    }

    public void setGlobalAnnounceServersError(String text) {
        if (!StringUtils.equals(errorGlobalDiscoverServers, text)) {
            errorGlobalDiscoverServers = text;
            notifyChange(syncthing.android.BR.globalAnnounceServersError);
        }
    }

    boolean validateGlobalDiscoveryServers(CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            //TODO
            return false;
        }
        return true;
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

    public final Action1<CharSequence> actionSetGuiListenAddress = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setGuiListenAddress(charSequence);
        }
    };

    @Bindable
    public String getGuiListenAddressError() {
        return errorGuiListenAddress;
    }

    public void setGuiListenAddressError(String text) {
        if (!StringUtils.equals(errorGuiListenAddress, text)) {
            errorGuiListenAddress = text;
            notifyChange(syncthing.android.BR.guiListenAddressError);
        }
    }

    boolean validateGuiListenAddress(CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            //TODO notify
            return false;
        }
        return true;
    }

    @Bindable
    public String getGuiUser() {
        return guiConfig.user;
    }

    public void setGuiUser(CharSequence text) {
        guiConfig.user = StringUtils.isEmpty(text) ? "" : text.toString();
    }

    public final Action1<CharSequence> actionSetGuiUser = new Action1<CharSequence>() {
        @Override
        public void call(CharSequence charSequence) {
            setGuiUser(charSequence);
        }
    };

    public void setGuiPassword(CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            guiConfig.password = "";
        } else if (!StringUtils.equals(hiddenPass, text.toString())) {
            guiConfig.password = text.toString();
        }
    }

    public final Action1<CharSequence> actionSetGuiPassword =
            Action1IgnoreFirst.wrap(SettingsPresenter.this::setGuiPassword);

    @Bindable
    public boolean isUseTLS() {
        return guiConfig.useTLS;
    }

    public void setUseTLS(boolean enable) {
        guiConfig.useTLS = enable;
    }

    public final Action1<Boolean> actionSetUseTLS = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            setUseTLS(aBoolean);
        }
    };

    @Bindable
    public boolean isStartBrowser() {
        return options.startBrowser;
    }

    public void setStartBrowser(boolean enable) {
        options.startBrowser = enable;
    }

    public final Action1<Boolean> actionSetStartBrowser = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            setStartBrowser(aBoolean);
        }
    };

    @Bindable
    public boolean isURAccepted() {
        return options.urAccepted > 0;
    }

    public void setURAccepted(boolean enable) {
        options.urAccepted = enable ? 1 : -1;
    }

    public final Action1<Boolean> actionSetURAccepted = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            setURAccepted(aBoolean);
        }
    };

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
        SyncthingUtils.copyToClipboard(btn.getContext(),
                btn.getContext().getString(R.string.api_key),
                getApiKey());
    }

    public void regenApiKey(View btn) {
        String key = SyncthingUtils.randomString(32);
        setApiKey(key);
        notifyChange(syncthing.android.BR.apiKey);
    }

    public void saveConfig(View btn) {
        boolean invalid = false;
        invalid |= errorListenAddress != null;
        invalid |= errorGlobalDiscoverServers != null;
        invalid |= errorGuiListenAddress != null;
        if (invalid) {
            dialogPresenter.showDialog(context -> new AlertDialog.Builder(context)
                    .setTitle(R.string.input_error)
                    .setMessage(R.string.input_error_message)
                    .setPositiveButton(android.R.string.cancel, null)
                    .setNegativeButton(R.string.save, (d,w) -> {
                        saveConfig();
                    })
                    .create());
        } else {
            saveConfig();
        }
    }

    private void saveConfig() {
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
