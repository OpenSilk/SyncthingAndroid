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

import android.databinding.DataBindingUtil;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import org.opensilk.common.core.dagger2.ScreenScope;

import dagger.Module;
import dagger.Provides;
import syncthing.android.ui.binding.ViewBinder;

/**
 * Created by drew on 3/16/15.
 */
@Module
public class EditDeviceModule {

    final EditDeviceScreen screen;

    public EditDeviceModule(EditDeviceScreen screen) {
        this.screen = screen;
    }

    @Provides
    public EditPresenterConfig provideConfig() {
        return EditPresenterConfig.builder()
                .setDeviceId(screen.deviceId)
                .setIsAdd(screen.isAdd)
                .setCredentials(screen.credentials)
                .build();
    }

    @Provides @ScreenScope
    public ViewBinder providePresenerBinding(EditDevicePresenter presenter) {
        return new ViewBinder() {
            @Override
            public void bindView(View view) {
                syncthing.android.ui.sessionsettings.EditDeviceViewBinding binding = DataBindingUtil.bind(view, presenter);
                presenter.takeView((CoordinatorLayout) view);
                binding.setPresenter(presenter);
                binding.executePendingBindings();
            }
        };
    }
}
