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

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mortar.dagger2support.DaggerService;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;

/**
 * Created by drew on 3/17/15.
 */
public class SettingsScreenView extends ScrollView {

    @InjectView(R.id.edit_device_name) EditText editDeviceName;
    @InjectView(R.id.edit_protocol_listen_addr) EditText editProtocolListenAddrs;
    @InjectView(R.id.edit_incoming_rate_limit) EditText editIncomingRateLim;
    @InjectView(R.id.edit_outgoing_rate_limit) EditText editOutgoingRateLim;
    @InjectView(R.id.check_enable_upnp) CheckBox enableUpnp;
    @InjectView(R.id.check_global_discovery) CheckBox enableGlobalDiscovr;
    @InjectView(R.id.check_local_discovery) CheckBox enableLocalDiscovr;
    @InjectView(R.id.edit_global_discovery_server) EditText editGlobalDiscovrServr;
    @InjectView(R.id.edit_gui_listen_addr) EditText editGuiListenAddrs;
    @InjectView(R.id.edit_gui_user) EditText editGuiUsr;
    @InjectView(R.id.edit_gui_pass) EditText editGuiPass;
    @InjectView(R.id.check_use_https) CheckBox enableHttps;
    @InjectView(R.id.check_start_browser) CheckBox enableBrowser;
    @InjectView(R.id.check_usage_reporting) CheckBox enableUsageReporting;
    @InjectView(R.id.edit_apikey) EditText editApiKey;


    final SettingsPresenter presenter;

    AlertDialog errorDialog;

    DeviceConfig deviceConfig;
    GUIConfig guiConfig;
    OptionsConfig options;

    public SettingsScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            presenter = null;
        } else {
            presenter = DaggerService.<SettingsComponent>getDaggerComponent(getContext()).presenter();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        if (!isInEditMode()) {
            presenter.takeView(this);
        }
    }

    @OnClick(R.id.btn_generate_apikey)
    void regenApiKey() {
        String key = SyncthingUtils.randomString(32);
        guiConfig.apiKey = key;
        editApiKey.setText(key);
    }

    @OnClick(R.id.btn_cancel)
    void doCancel() {
        presenter.dismissDialog();
    }

    @OnClick(R.id.btn_save)
    void doSave() {
        deviceConfig.name = editDeviceName.getText().toString();
        if (!presenter.validateListenAddresses(editProtocolListenAddrs.getText().toString())) {
            return;
        }
        options.listenAddress = SyncthingUtils.rollArray(editProtocolListenAddrs.getText().toString());
        if (!presenter.validateMaxRecv(editIncomingRateLim.getText().toString())) {
            return;
        }
        options.maxRecvKbps = Integer.valueOf(editIncomingRateLim.getText().toString());
        if (!presenter.validateMaxSend(editOutgoingRateLim.getText().toString())) {
            return;
        }
        options.maxSendKbps = Integer.valueOf(editOutgoingRateLim.getText().toString());
        options.upnpEnabled = enableUpnp.isChecked();
        options.globalAnnounceEnabled = enableGlobalDiscovr.isChecked();
        options.localAnnounceEnabled = enableLocalDiscovr.isChecked();
        if (!presenter.validateGlobalDiscoveryServers(editGlobalDiscovrServr.getText().toString())) {
            return;
        }
        options.globalAnnounceServers = SyncthingUtils.rollArray(editGlobalDiscovrServr.getText().toString());
        guiConfig.address = editGuiListenAddrs.getText().toString();
        guiConfig.user = editGuiUsr.getText().toString();
        if (!StringUtils.isEmpty(editGuiPass.getText().toString())) {
            guiConfig.password = editGuiPass.getText().toString();
        }
        guiConfig.useTLS = enableHttps.isChecked();
        options.startBrowser = enableBrowser.isChecked();
        options.urAccepted = (enableUsageReporting.isChecked()) ? 1000 : -1;
        guiConfig.apiKey = editApiKey.getText().toString();

        presenter.saveConfig(deviceConfig, options, guiConfig);
    }

    void initialize(DeviceConfig thisDevice, OptionsConfig options, GUIConfig guiConfig, boolean fromsavedstate) {
        this.deviceConfig = thisDevice;
        this.options = options;
        this.guiConfig = guiConfig;
        if (fromsavedstate) return;
        editDeviceName.setText(SyncthingUtils.getDisplayName(thisDevice));
        editProtocolListenAddrs.setText(SyncthingUtils.unrollArray(options.listenAddress));
        editIncomingRateLim.setText(String.valueOf(options.maxRecvKbps));
        editOutgoingRateLim.setText(String.valueOf(options.maxSendKbps));
        enableUpnp.setChecked(options.upnpEnabled);
        enableGlobalDiscovr.setChecked(options.globalAnnounceEnabled);
        enableLocalDiscovr.setChecked(options.localAnnounceEnabled);
        editGlobalDiscovrServr.setText(SyncthingUtils.unrollArray(options.globalAnnounceServers));
        editGuiListenAddrs.setText(guiConfig.address);
        editGuiUsr.setText(guiConfig.user);
        //ignoring password
        enableHttps.setChecked(guiConfig.useTLS);
        enableBrowser.setChecked(options.startBrowser);
        enableUsageReporting.setChecked(options.urAccepted >= 0);
        editApiKey.setText(guiConfig.apiKey);
    }

    void showError(String msg) {
        dismissErrorDialog();
        errorDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.error)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    void dismissErrorDialog() {
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
        }
    }

    void showConfigSaved() {
        Toast.makeText(getContext(), R.string.config_saved, Toast.LENGTH_SHORT).show();
    }

}
