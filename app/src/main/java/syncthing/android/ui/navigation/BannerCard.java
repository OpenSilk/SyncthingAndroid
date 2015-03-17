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

import syncthing.android.R;
import syncthing.android.ui.common.Card;

/**
 * Created by drew on 3/10/15.
 */
public class BannerCard extends Card {

    public static final BannerCard INSTANCE = new BannerCard();

    @Override
    public int getLayout() {
        return R.layout.navigation_banner;
    }
}
