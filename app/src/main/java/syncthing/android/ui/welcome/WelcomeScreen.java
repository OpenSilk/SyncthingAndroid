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

import android.content.res.Resources;
import android.support.v4.app.FragmentManager;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ComponentFactory;
import org.opensilk.common.ui.mortar.Layout;
import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.common.ui.mortar.WithComponentFactory;

import mortar.MortarScope;
import syncthing.android.R;
import syncthing.android.ui.login.LoginActivityComponent;

@Layout(R.layout.screen_welcome)
@WithComponentFactory(WelcomeScreen.Factory.class)
public class WelcomeScreen extends Screen {
    final FragmentManager fragmentManager;

    public WelcomeScreen(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public static class Factory extends ComponentFactory<WelcomeScreen> {
        @Override
        protected Object createDaggerComponent(Resources resources, MortarScope parentScope, WelcomeScreen screen) {
            LoginActivityComponent component = DaggerService.getDaggerComponent(parentScope);
            return WelcomeComponent.FACTORY.call(component, screen);
        }
    }
}
