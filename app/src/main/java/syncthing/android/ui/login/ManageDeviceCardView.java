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

package syncthing.android.ui.login;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mortar.dagger2support.DaggerService;
import rx.Subscription;
import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.android.ui.common.BindsCard;
import syncthing.android.ui.common.Card;

/**
 * Created by drew on 3/15/15.
 */
public class ManageDeviceCardView extends LinearLayout implements BindsCard {

    @InjectView(R.id.identicon) ImageView identicon;
    @InjectView(R.id.name) TextView name;
    @InjectView(R.id.check) View check;

    Credentials credentials;
    Subscription identiconSubscription;

    final ManagePresenter presenter;

    public ManageDeviceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<ManageComponent>getDaggerComponent(getContext()).presenter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (identiconSubscription != null) {
            identiconSubscription.unsubscribe();
        }
    }

    @OnClick(R.id.overflow)
    void showPopup(View v) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.inflate(R.menu.popup_login_manage);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.make_default:
                        if (credentials != null) {
                            presenter.setAsDefault(credentials);
                        }
                        return true;
                    case R.id.edit:
                        if (credentials != null) {
                            presenter.openEditScreen(credentials);
                        }
                        return true;
                    case R.id.remove:
                        if (credentials != null) {
                            presenter.removeDevice(credentials);
                        }
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public void bind(Card card) {
        ManageDeviceCard mdc = (ManageDeviceCard) card;
        credentials = mdc.credentials;
        name.setText(credentials.alias);
        identiconSubscription = presenter.identiconGenerator.generateAsync(credentials.id)
                .subscribe(identicon::setImageBitmap);
        check.setVisibility(mdc.isChecked() ? VISIBLE : GONE);
    }
}
