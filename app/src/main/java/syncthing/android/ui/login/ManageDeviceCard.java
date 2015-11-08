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

import android.databinding.Bindable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.PopupMenu;

import syncthing.android.R;
import syncthing.api.Credentials;
import syncthing.android.ui.common.Card;

/**
 * Created by drew on 3/15/15.
 */
public class ManageDeviceCard extends Card implements Checkable {

    private final ManagePresenter presenter;
    private final Credentials credentials;

    boolean checked = false;

    public ManageDeviceCard(ManagePresenter presenter, Credentials credentials) {
        this.presenter = presenter;
        this.credentials = credentials;
    }

    @Override
    public int getLayout() {
        return R.layout.login_manage_device;
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        notifyChange(syncthing.android.BR.checked);
    }

    @Override
    @Bindable
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    @Bindable
    public String getDeviceID() {
        return credentials.id;
    }

    @Bindable
    public String getName() {
        return credentials.alias;
    }

    public void showOverflowMenu(View v) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.inflate(R.menu.popup_login_manage);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.make_default:
                        presenter.setAsDefault(credentials);
                        return true;
                    case R.id.edit:
                        presenter.openEditScreen(credentials);
                        return true;
                    case R.id.remove:
                        presenter.removeDevice(credentials);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }
}
