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
import java.util.Collection;
import java.util.List;

import syncthing.android.model.Credentials;
import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.CardRecyclerAdapter;

/**
 * Created by drew on 3/15/15.
 */
public class ManageScreenAdapter extends CardRecyclerAdapter {

    List<ManageDeviceCard> devices = new ArrayList<>();

    public ManageScreenAdapter() {
    }

    void setDevices(Collection<Credentials> credentialses, Credentials def) {
        devices.clear();
        for (Credentials creds : credentialses) {
            ManageDeviceCard card = new ManageDeviceCard(creds);
            if (creds.equals(def)) {
                card.setChecked(true);
            }
            devices.add(card);
        }
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
