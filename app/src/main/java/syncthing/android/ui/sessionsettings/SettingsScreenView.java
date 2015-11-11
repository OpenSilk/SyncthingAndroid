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

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by drew on 3/17/15.
 */
public class SettingsScreenView extends CoordinatorLayout {

    @Inject SettingsPresenter mPresenter;
    CompositeSubscription subscriptions;
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
        if (!isInEditMode()) {
            binding = DataBindingUtil.bind(this);
            mPresenter.takeView(this);
            binding.setPresenter(mPresenter);
            binding.executePendingBindings();
            subscribeChanges();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        if (subscriptions != null) subscriptions.unsubscribe();
    }

    void subscribeChanges() {
        subscriptions = new CompositeSubscription(
                RxTextView.textChanges(binding.editDeviceName)
                        .subscribe(mPresenter::setDeviceName),
                RxTextView.textChanges(binding.editProtocolListenAddr)
                        .subscribe(mPresenter::setListenAddress),
                RxTextView.textChanges(binding.editIncomingRateLimit)
                        .subscribe(mPresenter::setMaxRecvKpbs),
                RxTextView.textChanges(binding.editOutgoingRateLimit)
                        .subscribe(mPresenter::setMaxSendKbps),
                RxCompoundButton.checkedChanges(binding.checkEnableUpnp)
                        .subscribe(mPresenter::setUpnpEnabled),
                RxCompoundButton.checkedChanges(binding.checkGlobalDiscovery)
                        .subscribe(mPresenter::setGlobalAnnounceEnabled),
                RxCompoundButton.checkedChanges(binding.checkLocalDiscovery)
                        .subscribe(mPresenter::setLocalAnnounceEnabled),
                RxTextView.textChanges(binding.editGlobalDiscoveryServer)
                        .subscribe(mPresenter::setGlobalAnnounceServers),
                RxTextView.textChanges(binding.editGuiListenAddr)
                        .subscribe(mPresenter::setGuiListenAddress),
                RxTextView.textChanges(binding.editGuiUser)
                        .subscribe(mPresenter::setGuiUser),
                RxTextView.textChanges(binding.editGuiPass)
                        .subscribe(mPresenter::setGuiPassword),
                RxCompoundButton.checkedChanges(binding.checkUseHttps)
                        .subscribe(mPresenter::setUseTLS),
                RxCompoundButton.checkedChanges(binding.checkStartBrowser)
                        .subscribe(mPresenter::setStartBrowser),
                RxCompoundButton.checkedChanges(binding.checkUsageReporting)
                        .subscribe(mPresenter::setURAccepted)
        );
    }

}
