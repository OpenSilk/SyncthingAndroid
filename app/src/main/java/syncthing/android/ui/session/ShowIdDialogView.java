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
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import syncthing.android.R;

/**
 * Created by drew on 3/11/15.
 */
public class ShowIdDialogView extends FrameLayout {

    @InjectView(R.id.id) TextView deviceId;
    @InjectView(R.id.qr_image) ImageView qrImage;
    @InjectView(R.id.loading_progress) ProgressBar progress;

    @Inject SessionPresenter mPresenter;

    String id;
    Subscription qrImageSubscription;

    public ShowIdDialogView(Context context, AttributeSet attrs) {
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
        if (isInEditMode()) {
            return;
        }
        final String id = mPresenter.controller.getMyID();
        if (!StringUtils.isEmpty(id)) {
            deviceId.setText(id);
            qrImageSubscription = mPresenter.controller.getQRImage(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            bitmap -> {
                                progress.setVisibility(GONE);
                                qrImage.setImageBitmap(bitmap);
                            },
                            t -> {
                                progress.setVisibility(GONE);
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

}
