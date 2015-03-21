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

import rx.functions.Action1;
import syncthing.android.R;
import syncthing.android.ui.common.Card;

/**
 * Created by drew on 3/1/15.
 */
public class HeaderCard extends Card {

    public static final HeaderCard FOLDER = new HeaderCard(
            R.string.folders,
            R.string.add_folder,
            SessionPresenter::openAddFolderScreen
    );

    public static final HeaderCard DEVICE = new HeaderCard(
            R.string.devices,
            R.string.add_device,
            SessionPresenter::openAddDeviceScreen
    );

    public final int title;
    public final int buttonText;
    public final Action1<SessionPresenter> addAction;

    public HeaderCard(int title, int buttonText, Action1<SessionPresenter> addAction) {
        this.title = title;
        this.buttonText = buttonText;
        this.addAction = addAction;
    }

    @Override
    public int getLayout() {
        return R.layout.session_header;
    }

    @Override
    public int adapterId() {
        return super.adapterId() ^ title;
    }
}
