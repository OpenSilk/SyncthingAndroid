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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.CardRecyclerAdapter;

/**
 * Created by drew on 3/15/15.
 */
public class ManageScreenAdapter extends CardRecyclerAdapter {

    final ManagePresenter presenter;
    final List<ManageDeviceCard> devices = new ArrayList<>();

    @Inject
    public ManageScreenAdapter(ManagePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public android.databinding.DataBindingComponent getBindingComponent() {
        return presenter;
    }

    public void replaceAll(List<ManageDeviceCard> cards) {
        devices.clear();
        addAll(cards);
    }

    public void addAll(List<ManageDeviceCard> cards) {
        devices.addAll(cards);
        notifyDataSetChanged();
    }

    @Override
    public Card getItem(int pos) {
        return devices.get(pos);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
