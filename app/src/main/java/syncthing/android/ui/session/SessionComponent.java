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

package syncthing.android.ui.session;

import org.opensilk.common.mortar.ActivityResultsController;

import syncthing.android.ui.LauncherActivityComponent;
import syncthing.api.SessionController;
import syncthing.api.SessionScope;
import syncthing.api.SyncthingApiLongpollModule;
import syncthing.api.SyncthingApiModule;

/**
* Created by drew on 3/11/15.
*/
@SessionScope
@dagger.Component(
        dependencies = LauncherActivityComponent.class,
        modules = {
                SessionModule.class,
                SyncthingApiModule.class,
                SyncthingApiLongpollModule.class
        }
)
public interface SessionComponent {
    SessionController sessionController();
    SessionPresenter presenter();
    SessionFragmentPresenter fragmentPresenter();
    ActivityResultsController activityResultsController();
}
