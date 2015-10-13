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
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.opensilk.common.core.mortar.DaggerService;

import java.text.DecimalFormat;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Subscription;
import rx.functions.Action1;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ExpandableCardViewWrapper;
import syncthing.api.model.ConnectionInfo;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.SystemInfo;
import syncthing.api.model.Version;

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

    @Inject SessionPresenter mPresenter;

    public MyDeviceCardView(Context context, AttributeSet attrs) {
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


    @Override
    public View getExpandView() {
        return expand;
    }

    public void onBind(MyDeviceCard card) {

    }

}
