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
import android.view.View;

import com.google.gson.annotations.Since;

import org.opensilk.common.core.dagger2.ScreenScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by drew on 3/16/15.
 */
@Module
public class EditFolderModule {

    final EditFolderScreen screen;

    public EditFolderModule(EditFolderScreen screen) {
        this.screen = screen;
    }

    @Provides
    public EditPresenterConfig provideConfig() {
        return EditPresenterConfig.builder()
                .setFolderId(screen.folderId)
                .setDeviceId(screen.deviceId)
                .setIsAdd(screen.isAdd)
                .setCredentials(screen.credentials)
                .build();
    }

    @Provides @ScreenScope
    public EditPresenterBinding providePresenerBinding(EditFolderPresenter presenter) {
        return new EditPresenterBinding() {
            @Override
            public void bindView(View view) {
                syncthing.android.ui.sessionsettings.EditFolderScreenViewBinding binding = DataBindingUtil.bind(view, presenter);
                presenter.takeView((EditFolderScreenView) view);
                binding.setPresenter(presenter);
                binding.editFolderPath.setAdapter(new EditFolderPresenter.DirectoryAutoCompleteAdapter(view.getContext(), presenter));
                binding.executePendingBindings();
            }
        };
    }

}
