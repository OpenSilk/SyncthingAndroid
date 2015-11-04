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

package syncthing.android.ui.folderpicker;

import android.content.res.Resources;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ComponentFactory;
import org.opensilk.common.ui.mortar.Layout;
import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.common.ui.mortar.WithComponentFactory;

import mortar.MortarScope;
import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.android.ui.SyncthingActivityComponent;

/**
 * Created by drew on 11/3/15.
 */
@Layout(R.layout.screen_folderpicker)
@WithComponentFactory(FolderPickerScreen.Factory.class)
public class FolderPickerScreen extends Screen {
    final Credentials credentials;
    final String path;

    public FolderPickerScreen(Credentials credentials, String path) {
        this.credentials = credentials;
        this.path = path;
    }

    @Override
    public String getName() {
        return super.getName() + "-" + path;
    }

    public static class Factory extends ComponentFactory<FolderPickerScreen> {
        @Override
        protected Object createDaggerComponent(Resources resources, MortarScope parentScope, FolderPickerScreen screen) {
            SyncthingActivityComponent parent = DaggerService.getDaggerComponent(parentScope);
            return FolderPickerComponent.FACTORY.call(parent, screen);
        }
    }
}
