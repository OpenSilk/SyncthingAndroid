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
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mortar.dagger2support.DaggerService;
import rx.Subscription;
import rx.functions.Action1;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.CardViewWrapper;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;

/**
 * Created by drew on 3/4/15.
 */
public class MyDeviceCardView extends CardViewWrapper {

    @InjectView(R.id.identicon) ImageView identicon;
    @InjectView(R.id.name) TextView name;
    @InjectView(R.id.expand) ViewGroup expand;
    @InjectView(R.id.download) TextView download;
    @InjectView(R.id.upload) TextView upload;
    @InjectView(R.id.mem_usage) TextView memory;
    @InjectView(R.id.cpu_usage) TextView cpu;
    @InjectView(R.id.global_discovery_container) ViewGroup globalDiscoveryHider;
    @InjectView(R.id.global_discovery) TextView globalDiscovery;
    @InjectView(R.id.version) TextView version;

    final SessionPresenter presenter;
    final DecimalFormat cpuFormat;

    String deviceId;
    Subscription identiconSubscription;

    public MyDeviceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cpuFormat = new DecimalFormat("0.00");
        if (isInEditMode()) {
            presenter = null;
        } else {
            presenter = DaggerService.<SessionComponent>getDaggerComponent(getContext()).presenter();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (identiconSubscription != null)
            identiconSubscription.unsubscribe();
    }

    @OnClick(R.id.header)
    void toggleExpand() {
        toggleExpanded();
    }

    @Override
    public View getExpandView() {
        return expand;
    }

    public void bind(Card card) {
        MyDeviceCard myDeviceCard = (MyDeviceCard) card;
        updateDevice(myDeviceCard.device);
        updateConnection(myDeviceCard.connection);
        updateSystem(myDeviceCard.system);
        updateVersion(myDeviceCard.version);
    }

    void updateDevice(DeviceConfig device) {
        if (device == null) {
            return;
        }
        if (!StringUtils.equals(this.deviceId, device.deviceID)) {
            identiconSubscription = presenter.identiconGenerator.generateAsync(device.deviceID)
                    .subscribe(new Action1<Bitmap>() {
                        @Override
                        public void call(Bitmap bitmap) {
                            identicon.setImageBitmap(bitmap);
                        }
                    });
        }

        this.deviceId = device.deviceID;
        name.setText(SyncthingUtils.getDisplayName(device));
    }

    public void updateConnection(ConnectionInfo conn) {
        if (conn == null) {
            download.setText(R.string.na);
            upload.setText(R.string.na);
            return;
        }
        download.setText(getContext().getString(
                R.string.transfer_rate_total,
                SyncthingUtils.readableTransferRate(getContext(), conn.inbps),
                SyncthingUtils.readableFileSize(getContext(), conn.inBytesTotal)
        ));
        upload.setText(getContext().getString(
                R.string.transfer_rate_total,
                SyncthingUtils.readableTransferRate(getContext(), conn.outbps),
                SyncthingUtils.readableFileSize(getContext(), conn.outBytesTotal)
        ));
    }

    public void updateSystem(SystemInfo sys) {
        if (sys == null) {
            return;
        }
        memory.setText(SyncthingUtils.readableFileSize(getContext(), sys.sys));
        cpu.setText(getResources().getString(R.string.cpu_percent, cpuFormat.format(sys.cpuPercent)));

        if (sys.extAnnounceOK != null && sys.announceServersTotal > 0) {
            globalDiscoveryHider.setVisibility(VISIBLE);
            if (sys.announceServersFailed.isEmpty()) {
                globalDiscovery.setText(android.R.string.ok);
                globalDiscovery.setTextColor(getResources().getColor(R.color.announce_ok));
            } else {
                globalDiscovery.setText(getResources().getString(R.string.announce_failures,
                        (sys.announceServersTotal - sys.announceServersFailed.size()),
                        sys.announceServersTotal
                ));
                globalDiscovery.setTextColor(getResources().getColor(R.color.announce_fail));
            }
        } else {
            globalDiscoveryHider.setVisibility(GONE);
        }
    }

    void updateVersion(Version ver) {
        version.setText(ver.version);
    }

}
