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

package syncthing.android.ui.navigation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mortar.dagger2support.DaggerService;
import rx.Subscription;
import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.android.ui.common.BindsCard;
import syncthing.android.ui.common.Card;

/**
 * Created by drew on 3/10/15.
 */
public class DeviceCardView extends LinearLayout implements BindsCard, View.OnClickListener {

    @InjectView(R.id.identicon) ImageView identicon;
    @InjectView(R.id.device_title) TextView title;

    final NavigationPresenter presenter;

    DeviceCard item;
    Subscription identiconSubscription;

    public DeviceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<NavigationComponent>getDaggerComponent(getContext()).presenter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        setOnClickListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (identiconSubscription != null) {
            identiconSubscription.unsubscribe();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == this && item != null) {
            presenter.openSessionScreen(item.credentials);
        }
    }

    @Override
    public void bind(Card card) {
        DeviceCard newItem = (DeviceCard) card;
        Credentials creds = newItem.credentials;
        title.setText(creds.alias);
        if (item == null || !StringUtils.equals(creds.id, item.credentials.id)) {
            identiconSubscription = presenter.identiconGenerator.generateAsync(creds.id)
                    .subscribe(identicon::setImageBitmap);
        }
        item = newItem;
    }

}
