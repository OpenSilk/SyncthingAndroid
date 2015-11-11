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

package syncthing.android.ui.sessionsettings;

import android.view.View;

import org.opensilk.common.core.dagger2.ScreenScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by drew on 10/11/15.
 */
@Module
public class SettingsModule {
    final SettingsScreen screen;

    public SettingsModule(SettingsScreen screen) {
        this.screen = screen;
    }

    @Provides @ScreenScope
    public EditPresenterConfig provideConfig() {
        EditPresenterConfig c = EditPresenterConfig.builder()
                .setCredentials(screen.credentials)
                .build();
        return c;
    }

    @Provides @ScreenScope
    public EditPresenterBinding providePresenerBinding(SettingsPresenter presenter) {
        return new EditPresenterBinding() {
            @Override
            public void bindView(View view) {

            }
        };
    }
}
