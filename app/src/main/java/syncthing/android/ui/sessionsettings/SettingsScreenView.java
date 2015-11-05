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
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;

/**
 * Created by drew on 3/17/15.
 */
public class SettingsScreenView extends CoordinatorLayout {

    @InjectView(R.id.toolbar) Toolbar toolbar;
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
    @InjectView(R.id.btn_copy_apikey) Button copyApiKeyButton;

    @Inject ToolbarOwner mToolbarOwner;
    @Inject SettingsPresenter mPresenter;

    DeviceConfig deviceConfig;
    GUIConfig guiConfig;
    OptionsConfig options;

    String hiddenPass = SyncthingUtils.hiddenString(20);

    public SettingsScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            SettingsComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        if (!isInEditMode()) {
            if (!SyncthingUtils.isClipBoardSupported(getContext())) {
                copyApiKeyButton.setVisibility(GONE);
            }
            mPresenter.takeView(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mToolbarOwner.attachToolbar(toolbar);
            mToolbarOwner.setConfig(mPresenter.getToolbarConfig());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        mToolbarOwner.detachToolbar(toolbar);
    }

    @OnClick(R.id.btn_copy_apikey)
    void copyApiKey() {
        SyncthingUtils.copyToClipboard(getContext(),
                getContext().getString(R.string.api_key), editApiKey.getText().toString());
    }

    @OnClick(R.id.btn_generate_apikey)
    void regenApiKey() {
        String key = SyncthingUtils.randomString(32);
        guiConfig.apiKey = key;
        editApiKey.setText(key);
    }

    @OnClick(R.id.btn_cancel)
    void doCancel() {
        mPresenter.dismissDialog();
    }

    @OnClick(R.id.btn_save)
    void doSave() {
        deviceConfig.name = editDeviceName.getText().toString();
        if (!mPresenter.validateListenAddresses(editProtocolListenAddrs.getText().toString())) {
            return;
        }
        options.listenAddress = SyncthingUtils.rollArray(editProtocolListenAddrs.getText().toString());
        if (!mPresenter.validateMaxRecv(editIncomingRateLim.getText().toString())) {
            return;
        }
        options.maxRecvKbps = Integer.valueOf(editIncomingRateLim.getText().toString());
        if (!mPresenter.validateMaxSend(editOutgoingRateLim.getText().toString())) {
            return;
        }
        options.maxSendKbps = Integer.valueOf(editOutgoingRateLim.getText().toString());
        options.upnpEnabled = enableUpnp.isChecked();
        options.globalAnnounceEnabled = enableGlobalDiscovr.isChecked();
        options.localAnnounceEnabled = enableLocalDiscovr.isChecked();
        if (!mPresenter.validateGlobalDiscoveryServers(editGlobalDiscovrServr.getText().toString())) {
            return;
        }
        options.globalAnnounceServers = SyncthingUtils.rollArray(editGlobalDiscovrServr.getText().toString());
        guiConfig.address = editGuiListenAddrs.getText().toString();
        guiConfig.user = editGuiUsr.getText().toString();
        if (!StringUtils.equals(editGuiPass.getText().toString(), hiddenPass)) {
            guiConfig.password = editGuiPass.getText().toString();
        }
        guiConfig.useTLS = enableHttps.isChecked();
        options.startBrowser = enableBrowser.isChecked();
        options.urAccepted = (enableUsageReporting.isChecked()) ? 1000 : -1;
        guiConfig.apiKey = editApiKey.getText().toString();

        mPresenter.saveConfig(deviceConfig, options, guiConfig);
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
        editGuiPass.setText(hiddenPass);
        enableHttps.setChecked(guiConfig.useTLS);
        enableBrowser.setChecked(options.startBrowser);
        enableUsageReporting.setChecked(options.urAccepted >= 0);
        editApiKey.setText(guiConfig.apiKey);
    }

}
