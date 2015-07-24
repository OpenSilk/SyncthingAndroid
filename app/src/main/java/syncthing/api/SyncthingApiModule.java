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

import java.util.concurrent.Executor;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.converter.Converter;

/**
 * Created by drew on 3/4/15.
 */
@Module
public class SyncthingApiModule {

    String caCert;

    public SyncthingApiModule(String caCert) {
        this.caCert = caCert;
    }

    @Provides @SessionScope
    public SyncthingApi provideSyncthingApi(Endpoint endpoint,
                                            RequestInterceptor interceptor,
                                            Converter converter,
                                            Client client,
                                            Executor httpExecutor) {
        // Hack to update the Http client with CA Certs
        ((OkClient)client).setSslSocketFactory(
                SyncthingSSLSocketFactory.createSyncthingSSLSocketFactory(caCert));
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(converter)
                .setClient(client)
                .setExecutors(httpExecutor, null)
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setRequestInterceptor(interceptor)
                .build();
        return adapter.create(SyncthingApi.class);
    }

}
