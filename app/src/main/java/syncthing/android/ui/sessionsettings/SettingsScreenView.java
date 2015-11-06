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
import android.databinding.DataBindingUtil;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import javax.inject.Inject;

import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.GUIConfig;
import syncthing.api.model.OptionsConfig;

/**
 * Created by drew on 3/17/15.
 */
public class SettingsScreenView extends CoordinatorLayout {

    @Inject ToolbarOwner mToolbarOwner;
    @Inject SettingsPresenter mPresenter;

    syncthing.android.ui.sessionsettings.SettingsScreenViewBinding binding;

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
        binding = DataBindingUtil.bind(this);
        binding.setPresenter(mPresenter);
        if (!isInEditMode()) {
            if (!SyncthingUtils.isClipBoardSupported(getContext())) {
                binding.btnCopyApikey.setVisibility(GONE);
            }
            mPresenter.takeView(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mToolbarOwner.attachToolbar(binding.toolbar);
            mToolbarOwner.setConfig(mPresenter.getToolbarConfig());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        mToolbarOwner.detachToolbar(binding.toolbar);
    }

    void initialize(DeviceConfig thisDevice, OptionsConfig options, GUIConfig guiConfig, boolean fromsavedstate) {
        if (fromsavedstate) return;
        String hiddenPass = mPresenter.hiddenPass;
        binding.editDeviceName.setText(SyncthingUtils.getDisplayName(thisDevice));
        binding.editProtocolListenAddr.setText(SyncthingUtils.unrollArray(options.listenAddress));
        binding.editIncomingRateLimit.setText(String.valueOf(options.maxRecvKbps));
        binding.editOutgoingRateLimit.setText(String.valueOf(options.maxSendKbps));
        binding.checkEnableUpnp.setChecked(options.upnpEnabled);
        binding.checkGlobalDiscovery.setChecked(options.globalAnnounceEnabled);
        binding.checkLocalDiscovery.setChecked(options.localAnnounceEnabled);
        binding.editGlobalDiscoveryServer.setText(SyncthingUtils.unrollArray(options.globalAnnounceServers));
        binding.editGuiListenAddr.setText(guiConfig.address);
        binding.editGuiUser.setText(guiConfig.user);
        binding.editGuiPass.setText(hiddenPass);
        binding.checkEnableUpnp.setChecked(guiConfig.useTLS);
        binding.checkStartBrowser.setChecked(options.startBrowser);
        binding.checkUsageReporting.setChecked(options.urAccepted >= 0);
        binding.editApikey.setText(guiConfig.apiKey);
    }

}
