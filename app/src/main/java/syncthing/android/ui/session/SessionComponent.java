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

import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.ui.mortar.ActivityResultsController;

import rx.functions.Func2;
import syncthing.android.ui.LauncherActivityComponent;
import syncthing.api.SessionController;
import syncthing.api.SessionScope;
import syncthing.api.SyncthingApiLongpollModule;
import syncthing.api.SyncthingApiModule;

/**
* Created by drew on 3/11/15.
*/
@ScreenScope
@dagger.Component(
        dependencies = LauncherActivityComponent.class,
        modules = {
                SessionModule.class,
        }
)
public interface SessionComponent {
    Func2<LauncherActivityComponent, SessionScreen, SessionComponent> FACTORY =
            new Func2<LauncherActivityComponent, SessionScreen, SessionComponent>() {
                @Override
                public SessionComponent call(LauncherActivityComponent launcherActivityComponent, SessionScreen sessionScreen) {
                    return DaggerSessionComponent.builder()
                            .launcherActivityComponent(launcherActivityComponent)
                            .sessionModule(new SessionModule(sessionScreen))
                            .build();
                }
            };
    SessionPresenter presenter();
    ActivityResultsController activityResultsController();
    void inject(DeviceCardView view);
    void inject(FolderCardView view);
    void inject(HeaderCardView view);
    void inject(MyDeviceCardView view);
    void inject(NotifCardErrorView view);
    void inject(NotifCardRejDeviceView view);
    void inject(NotifCardRejFolderView view);
    void inject(NotifCardRestartView view);
    void inject(SessionScreenView view);
    void inject(SessionScreenViewLand view);
    void inject(ShowIdDialogView view);
}
