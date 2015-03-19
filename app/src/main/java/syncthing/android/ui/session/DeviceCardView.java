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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mortar.dagger2support.DaggerService;
import rx.Subscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.CardViewWrapper;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.DeviceStats;

/**
 * Created by drew on 3/1/15.
 */
public class DeviceCardView extends CardViewWrapper {

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

    final SessionPresenter presenter;
    final DateTime epoch;

    String deviceId;
    Subscription identiconSubscription;

    public DeviceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<SessionComponent>getDaggerComponent(getContext()).presenter();
        epoch = new DateTime(1969);
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
        if (identiconSubscription != null) {
            identiconSubscription.unsubscribe();
        }
    }

    @OnClick(R.id.header)
    void toggleExpand() {
        toggleExpanded();
    }

    @OnClick(R.id.btn_edit)
    void editDevice() {
        if (deviceId == null) return;
        presenter.openEditDeviceScreen(deviceId);
    }

    public ViewGroup getExpandView() {
        return expand;
    }

    public void bind(Card card) {
        DeviceCard deviceCard = (DeviceCard) card;
        updateDevice(deviceCard.device);
        updateConnection(deviceCard.connection);
        updateStats(deviceCard.stats);
        updateCompletion(deviceCard.completion);
    }

    void updateDevice(DeviceConfig device) {
        if (!StringUtils.equals(this.deviceId, device.deviceID)) {
            identiconSubscription = presenter.identiconGenerator.generateAsync(device.deviceID)
                    .subscribe(identicon::setImageBitmap);
        }
        this.deviceId = device.deviceID;

        name.setText(SyncthingUtils.getDisplayName(device));

        compressionHider.setVisibility(VISIBLE);
        compression.setText(device.compression.localizedString(getContext()));
        introducerHider.setVisibility(device.introducer ? VISIBLE : GONE);
    }

    void updateConnection(ConnectionInfo conn) {
        if (conn == null) {
            downloadHider.setVisibility(GONE);
            uploadHider.setVisibility(GONE);
            addressHider.setVisibility(GONE);
            versionHider.setVisibility(GONE);
            lastSeenHider.setVisibility(VISIBLE);
        } else {
            download.setText(getContext().getString(
                    R.string.transfer_rate_total,
                    SyncthingUtils.readableTransferRate(getContext(), conn.inbps),
                    SyncthingUtils.readableFileSize(getContext(), conn.inBytesTotal)
            ));
            downloadHider.setVisibility(VISIBLE);

            upload.setText(getContext().getString(
                    R.string.transfer_rate_total,
                    SyncthingUtils.readableTransferRate(getContext(), conn.outbps),
                    SyncthingUtils.readableFileSize(getContext(), conn.outBytesTotal)
            ));
            uploadHider.setVisibility(VISIBLE);

            address.setText(conn.address);
            addressHider.setVisibility(VISIBLE);

            version.setText(conn.clientVersion);
            versionHider.setVisibility(VISIBLE);

            lastSeenHider.setVisibility(GONE);
        }
    }

    void updateStats(DeviceStats stats) {
        if (stats == null) {
            lastSeen.setText(R.string.unknown);
        } else {
            if (stats.lastSeen.year().equals(epoch.year())) {
                compressionHider.setVisibility(GONE);
                lastSeen.setText(R.string.never);
            } else {
                lastSeen.setText(stats.lastSeen.toString("yyyy-MM-dd HH:mm"));
            }
        }
    }

    void updateCompletion(int completion) {
        Resources r = getContext().getResources();
        if (completion < 0) {
            status.setText(R.string.disconnected);
            status.setTextColor(r.getColor(R.color.device_disconnected));
            progress.setVisibility(GONE);
        } else if (completion == 100) {
            status.setText(R.string.up_to_date);
            status.setTextColor(r.getColor(R.color.device_idle));
            progress.setVisibility(GONE);
        } else {
            status.setText(R.string.syncing);
            status.setTextColor(r.getColor(R.color.device_syncing));
            progress.setVisibility(VISIBLE);
            progress.setProgress(completion);
        }
    }

}
