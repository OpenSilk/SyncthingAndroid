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

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensilk.common.core.mortar.DaggerService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Subscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ExpandableCardViewWrapper;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.DeviceStats;

/**
 * Created by drew on 3/1/15.
 */
public class DeviceCardView extends ExpandableCardViewWrapper<DeviceCard> {

    @InjectView(R.id.identicon) ImageView identicon;
    @InjectView(R.id.name) TextView name;
    @InjectView(R.id.status) TextView status;
    @InjectView(R.id.progress) ProgressBar progress;
    @InjectView(R.id.expand) ViewGroup expand;
    @InjectView(R.id.download_container) ViewGroup downloadHider;
    @InjectView(R.id.download) TextView download;
    @InjectView(R.id.upload) TextView upload;
    @InjectView(R.id.upload_container) ViewGroup uploadHider;
    @InjectView(R.id.address_container) ViewGroup addressHider;
    @InjectView(R.id.address) TextView address;
    @InjectView(R.id.use_compression_container) ViewGroup compressionHider;
    @InjectView(R.id.compresion_type) TextView compression;
    @InjectView(R.id.introducer_container) ViewGroup introducerHider;
    @InjectView(R.id.version_container) ViewGroup versionHider;
    @InjectView(R.id.version) TextView version;
    @InjectView(R.id.last_seen_container) ViewGroup lastSeenHider;
    @InjectView(R.id.last_seen) TextView lastSeen;

    @Inject SessionPresenter mPresenter;

    Subscription identiconSubscription;
    Subscription connectionSubscription;
    Subscription statsSubscription;
    Subscription completionSubscription;

    public DeviceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            SessionComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @OnClick(R.id.btn_edit)
    void editDevice() {
        mPresenter.openEditDeviceScreen(getCard().device.deviceID);
    }

    public ViewGroup getExpandView() {
        return expand;
    }

    public void onBind(DeviceCard card) {
    }

}
