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

package syncthing.android.ui.welcome;

import android.content.Context;
import android.view.View;

import org.opensilk.common.ui.mortar.MortarPagerAdapter;
import org.opensilk.common.ui.mortar.Screen;

public class WelcomeScreenPagerAdapter extends MortarPagerAdapter<Screen, View> {

    final WelcomePresenter presenter;

    public WelcomeScreenPagerAdapter(Context context, WelcomePresenter presenter) {
        super(context, getScreens());
        this.presenter = presenter;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Welcome";
            case 1:
                return "Syncthing";
            case 2:
                return "Devices";
            case 3:
                return "Folders";
            case 4:
                return "Remote Control";
            case 5:
                return "Start";
        }
        return null;
    }

    static Screen[] getScreens() {
        return new Screen[] {
                new WelcomePage0(),
                new WelcomePage1(),
                new WelcomePage2(),
                new WelcomePage3(),
                new WelcomePage4(),
                new WelcomePage5(),
        };
    }

}
