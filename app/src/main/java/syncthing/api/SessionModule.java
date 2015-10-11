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

package syncthing.api;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.RequestInterceptor;
import syncthing.android.model.Credentials;

/**
 * Created by drew on 10/10/15.
 */
@Module
public class SessionModule {

    final Credentials credentials;

    public SessionModule(Credentials credentials) {
        this.credentials = credentials;
    }

    @Provides @SessionScope
    public Credentials provideCredentials(){
        return credentials;
    }

    @Provides @SessionScope
    public Endpoint provideEnpoint() {
        return credentials;
    }

    @Provides @SessionScope
    public RequestInterceptor provideRequestInterceptor() {
        return credentials;
    }
}
