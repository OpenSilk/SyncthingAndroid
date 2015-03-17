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

package syncthing.android.ui.login;

import java.util.concurrent.Executor;

import dagger.Provides;
import retrofit.Endpoint;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.converter.Converter;
import syncthing.android.model.Credentials;
import syncthing.api.SyncthingApi;

/**
* Created by drew on 3/11/15.
*/
@dagger.Module
public class LoginModule {
    final Credentials credentials;
    public LoginModule(LoginScreen screen) {
        credentials = screen.credentials;
    }

    @Provides
    public Credentials provideCredentials() {
        return credentials;
    }

    @Provides @LoginScreenScope
    public SyncthingApi provideLoginSynchingApi(Converter converter,
                                                Client client,
                                                Executor httpExecutor,
                                                Endpoint endpoint,
                                                RequestInterceptor requestInterceptor) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(converter)
                .setClient(client)
                .setExecutors(httpExecutor, null)
                .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
                .setRequestInterceptor(requestInterceptor)
                .build();
        return adapter.create(SyncthingApi.class);
    }

    @Provides @LoginScreenScope
    public Endpoint provideEndpoint(MovingEndpoint endpoint) {
        return endpoint;
    }

    @Provides @LoginScreenScope
    public RequestInterceptor provideRequestInterceptor(MovingRequestInterceptor interceptor) {
        return interceptor;
    }
}
