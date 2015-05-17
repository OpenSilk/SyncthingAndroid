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

package syncthing.android.service;

import dagger.Component;
import rx.functions.Func2;

/**
 * Created by drew on 3/21/15.
 */
@SyncthingInstanceScope
@Component (
        dependencies = ServiceComponent.class,
        modules = {
                SyncthingInstanceModule.class,
        }
)
public interface SyncthingInstanceComponent {
    Func2<ServiceComponent, SyncthingInstance, SyncthingInstanceComponent> FACTORY =
            new Func2<ServiceComponent, SyncthingInstance, SyncthingInstanceComponent>() {
                @Override
                public SyncthingInstanceComponent call(ServiceComponent serviceComponent, SyncthingInstance syncthingInstance) {
                    return DaggerSyncthingInstanceComponent.builder()
                            .serviceComponent(serviceComponent)
                            .syncthingInstanceModule(new SyncthingInstanceModule(syncthingInstance))
                            .build();
                }
            };
    void inject(SyncthingInstance service);
}
