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

import android.content.res.Resources;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ComponentFactory;
import org.opensilk.common.ui.mortar.Layout;
import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.common.ui.mortar.WithComponentFactory;

import mortar.MortarScope;
import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.android.ui.ManageActivityComponent;

/**
 * Created by drew on 3/23/15.
 */
@Layout(R.layout.screen_edit_folder_ignores)
@WithComponentFactory(EditIgnoresScreen.Factory.class)
public class EditIgnoresScreen extends Screen {
    final Credentials credentials;
    final String folderId;

    public EditIgnoresScreen(Credentials credentials, String folderId) {
        this.credentials = credentials;
        this.folderId = folderId;
    }

    @Override
    public String getName() {
        return super.getName() + folderId;
    }

    public static class Factory extends ComponentFactory<EditIgnoresScreen> {
        @Override
        protected Object createDaggerComponent(Resources resources, MortarScope parentScope, EditIgnoresScreen screen) {
            ManageActivityComponent cmp = DaggerService.getDaggerComponent(parentScope);
            return DaggerEditIgnoresComponent.builder()
                    .manageActivityComponent(cmp)
                    .editIgnoresModule(new EditIgnoresModule(screen))
                    .build();
        }
    }
}
