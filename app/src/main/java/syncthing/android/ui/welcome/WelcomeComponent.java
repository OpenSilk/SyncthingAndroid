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

import org.opensilk.common.core.dagger2.ScreenScope;

import rx.functions.Func2;
import syncthing.android.ui.ManageActivityComponent;

@ScreenScope
@dagger.Component(
        dependencies = ManageActivityComponent.class
)
public interface WelcomeComponent {
    Func2<ManageActivityComponent, WelcomeScreen, WelcomeComponent> FACTORY =
            new Func2<ManageActivityComponent, WelcomeScreen, WelcomeComponent>() {
                @Override
                public WelcomeComponent call(ManageActivityComponent loginActivityComponent, WelcomeScreen welcomeScreen) {
                    return DaggerWelcomeComponent.builder()
                            .manageActivityComponent(loginActivityComponent)
                            .build();
                }
            };

    void inject(WelcomeScreenView view);
    void inject(WelcomeScreenPagerView view);
    WelcomePresenter welcomePresenter();
}
