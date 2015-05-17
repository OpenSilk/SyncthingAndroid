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

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.DeviceConfig;

/**
 * Created by drew on 3/11/15.
 */
public class ShowIdDialogView extends RelativeLayout {

    @InjectView(R.id.id) TextView deviceId;
    @InjectView(R.id.qr_image) ImageView qrImage;
    @InjectView(R.id.loading_progress) ProgressBar progress;

    final SessionPresenter presenter;

    String id;
    Subscription qrImageSubscription;
    Dialog dialog;

    public ShowIdDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<SessionComponent>getDaggerComponent(getContext()).presenter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        DeviceConfig device = presenter.controller.getThisDevice();
        if (device != null && !StringUtils.isEmpty(device.deviceID)) {
            id = device.deviceID;
            deviceId.setText(device.deviceID);
            qrImageSubscription = presenter.controller.getQRImage(device.deviceID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            bitmap -> {
                                progress.setVisibility(GONE);
                                qrImage.setImageBitmap(bitmap);
                            },
                            t -> {
                                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    );
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (qrImageSubscription != null)
            qrImageSubscription.unsubscribe();
    }

    @OnClick(R.id.btn_copy)
    void copyDeviceId() {
        if (id == null) return;
        SyncthingUtils.copyToClipboard(getContext(),
                getContext().getString(R.string.device_id), id);
    }

    @OnClick(R.id.btn_share)
    void shareDeviceId() {
        if (id == null) return;
        SyncthingUtils.shareDeviceId(getContext(), id);
    }

    @OnClick(R.id.btn_close)
    void doClose() {
        if (dialog != null) dialog.dismiss();
    }

    void setDialog(Dialog d) {
        this.dialog = d;
    }
}
