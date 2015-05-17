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

import rx.functions.Func2;

/**
* Created by drew on 3/11/15.
*/
@LoginScreenScope
@dagger.Component(
        dependencies = LoginActivityComponent.class,
        modules = LoginModule.class
)
public interface LoginComponent {
    Func2<LoginActivityComponent, LoginScreen, LoginComponent> FACTORY =
            new Func2<LoginActivityComponent, LoginScreen, LoginComponent>() {
                @Override
                public LoginComponent call(LoginActivityComponent loginActivityComponent, LoginScreen loginScreen) {
                    return DaggerLoginComponent.builder()
                            .loginActivityComponent(loginActivityComponent)
                            .loginModule(new LoginModule(loginScreen))
                            .build();
                }
            };
    void inject(LoginScreenView view);
}
