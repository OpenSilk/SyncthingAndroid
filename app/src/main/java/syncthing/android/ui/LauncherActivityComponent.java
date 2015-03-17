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

package syncthing.android.ui;

import org.opensilk.common.dagger2.ActivityScope;
import org.opensilk.common.mortar.ActionBarOwner;
import org.opensilk.common.mortar.ActivityResultsController;
import org.opensilk.common.mortar.DrawerOwner;
import org.opensilk.common.mortarfragment.FragmentManagerOwner;

import syncthing.android.AppComponent;

/**
* Created by drew on 3/11/15.
*/
@ActivityScope
@dagger.Component(
        dependencies = AppComponent.class,
        modules = LauncherActivityModule.class
)
public interface LauncherActivityComponent extends AppComponent {
    void inject(LauncherActivity activity);
    ActionBarOwner actionBarOwner();
    DrawerOwner drawerOwner();
    FragmentManagerOwner fragmentManagerOwner();
    ActivityResultsController activityResultsController();
}
