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
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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
import syncthing.android.ui.common.ExpandableCardViewWrapper;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;
import timber.log.Timber;

/**
 * Created by drew on 3/4/15.
 */
public class MyDeviceCardView extends ExpandableCardViewWrapper<MyDeviceCard> {

    @InjectView(R.id.identicon) ImageView identicon;
    @InjectView(R.id.name) TextView name;
    @InjectView(R.id.expand) ViewGroup expand;
    @InjectView(R.id.download) TextView download;
    @InjectView(R.id.upload) TextView upload;
    @InjectView(R.id.mem_usage) TextView memory;
    @InjectView(R.id.cpu_usage) TextView cpu;
    @InjectView(R.id.global_discovery_container) ViewGroup globalDiscoveryHider;
    @InjectView(R.id.global_discovery) TextView globalDiscovery;
    @InjectView(R.id.uptime) TextView uptime;
    @InjectView(R.id.version) TextView version;

    final SessionPresenter presenter;
    final DecimalFormat cpuFormat;
    final PeriodFormatter uptimeFormatter;

    Subscription identiconSubscription;
    Subscription connectionSubscription;
    Subscription systemInfoSubscription;

    public MyDeviceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cpuFormat = new DecimalFormat("0.00");
        if (isInEditMode()) {
            presenter = null;
        } else {
            presenter = DaggerService.<SessionComponent>getDaggerComponent(getContext()).presenter();
        }
        uptimeFormatter = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix("d ")
                .appendHours()
                .appendSuffix("h ")
                .appendMinutes()
                .appendSuffix("m ")
                .appendSeconds()
                .appendSuffix("s")
                .toFormatter();
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
        unsubscribe();
    }

    @OnClick(R.id.header)
    void toggleExpand() {
        toggleExpanded();
    }

    @Override
    public View getExpandView() {
        return expand;
    }

    public void onBind(MyDeviceCard card) {
        updateDevice(card.device);
        updateConnection(card.connection);
        updateSystem(card.system);
        updateVersion(card.version);
        subscribeUpdates();
    }

    @Override
    public void reset() {
        super.reset();
        unsubscribe();
    }

    void updateDevice(DeviceConfig device) {
        if (device == null) {
            return;
        }
        identiconSubscription = presenter.identiconGenerator.generateAsync(device.deviceID)
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        identicon.setImageBitmap(bitmap);
                    }
                });
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
                SyncthingUtils.humanReadableTransferRate(conn.inbps),
                SyncthingUtils.humanReadableSize(conn.inBytesTotal)
        ));
        upload.setText(getContext().getString(
                R.string.transfer_rate_total,
                SyncthingUtils.humanReadableTransferRate(conn.outbps),
                SyncthingUtils.humanReadableSize(conn.outBytesTotal)
        ));
    }

    public void updateSystem(SystemInfo sys) {
        //Timber.d("updateSystem(%s) sys=%s", getCard().device.name, sys);
        if (sys == null) {
            return;
        }
        memory.setText(SyncthingUtils.humanReadableSize(sys.sys));
        cpu.setText(getResources().getString(R.string.cpu_percent, cpuFormat.format(sys.cpuPercent)));
        uptime.setText(uptimeFormatter.print(Duration.standardSeconds(sys.uptime).toPeriod()));

        if (sys.extAnnounceOK != null && sys.announceServersTotal > 0) {
            globalDiscoveryHider.setVisibility(VISIBLE);
            if (sys.announceServersFailed.isEmpty()) {
                globalDiscovery.setText(android.R.string.ok);
                globalDiscovery.setTextColor(getResources().getColor(R.color.announce_ok));
            } else {
                int failures = (sys.announceServersTotal - sys.announceServersFailed.size());
                globalDiscovery.setText(getResources().getString(R.string.announce_failures,
                        failures, sys.announceServersTotal
                ));
                globalDiscovery.setTextColor(getResources().getColor(
                                failures == sys.announceServersTotal
                                        ? R.color.announce_fail
                                        : R.color.announce_ok
                ));
            }
        } else {
            globalDiscoveryHider.setVisibility(GONE);
        }
    }

    void updateVersion(Version ver) {
        version.setText(ver.toString());
    }

    void subscribeUpdates() {
        unsubscribeUpdates();
        connectionSubscription = presenter.bus.subscribe(
                new Action1<Update.ConnectionInfo>() {
                    @Override
                    public void call(Update.ConnectionInfo conn) {
                        if (!StringUtils.equals(conn.id, getCard().device.deviceID)) {
                            return;
                        }
                        getCard().setConnectionInfo(conn.conn);
                        updateConnection(conn.conn);
                    }
                },
                Update.ConnectionInfo.class
        );
        systemInfoSubscription = presenter.bus.subscribe(
                new Action1<SystemInfo>() {
                    @Override
                    public void call(SystemInfo sys) {
                        getCard().setSystemInfo(sys);
                        updateSystem(sys);
                    }
                },
                SystemInfo.class
        );
    }

    void unsubscribe() {
        if (identiconSubscription != null) {
            identiconSubscription.unsubscribe();
        }
        unsubscribeUpdates();
    }

    void unsubscribeUpdates() {
        if (connectionSubscription != null) {
            connectionSubscription.unsubscribe();
        }
        if (systemInfoSubscription != null) {
            systemInfoSubscription.unsubscribe();
        }
    }

}
