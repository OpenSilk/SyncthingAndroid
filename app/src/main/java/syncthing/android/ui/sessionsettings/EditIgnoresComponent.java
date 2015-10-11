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

import org.opensilk.common.core.dagger2.ScreenScope;

import dagger.Component;
import syncthing.android.ui.ManageActivityComponent;
import syncthing.android.ui.session.SessionComponent;

/**
 * Created by drew on 3/23/15.
 */
@ScreenScope
@Component(
        dependencies = ManageActivityComponent.class,
        modules = EditIgnoresModule.class
)
//TODO this should really be a child of EditFolderComponent
public interface EditIgnoresComponent extends EditFragmentComponent {
    EditIgnoresPresenter presenter();
}
