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

import com.google.gson.Gson;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import syncthing.android.BuildConfig;

/**
 * Created by drew on 4/11/15.
 */
@Module
public class SyncthingApiLongpollModule {

    static final int LONGPOLL_CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int LONGPOLL_READ_TIMEOUT_MILLIS = 3 * 60 * 1000; // 3m //20 * 1000; // 20s
    static final int LONGPOLL_MAX_IDLE_CONNECTIONS = 2;
    static final long LONGPOLL_KEEP_ALIVE_DURATION_MS = 5 * 60 * 1000; // 5 min

    @Provides @SessionScope @Named("longpoll")
    public SyncthingApi provideSyncthingApi(
            Gson gson, OkHttpClient okClient, SyncthingApiConfig config
    ) {
        OkHttpClient client = okClient.clone();
        client.setConnectTimeout(LONGPOLL_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(LONGPOLL_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setConnectionPool(new ConnectionPool(LONGPOLL_MAX_IDLE_CONNECTIONS, LONGPOLL_KEEP_ALIVE_DURATION_MS));
        client.setSslSocketFactory(SyncthingSSLSocketFactory.create(config.getCACert()));
        client.setHostnameVerifier(new NullHostNameVerifier());
        client.interceptors().add(new SyncthingApiInterceptor(config));
        Retrofit.Builder b = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .baseUrl(config.getBaseUrl());
        if (BuildConfig.DEBUG) {
            b.validateEagerly();
        }
        return b.build().create(SyncthingApi.class);
    }
}
