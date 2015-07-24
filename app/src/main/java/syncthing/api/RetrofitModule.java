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
import com.squareup.okhttp.OkHttpClient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import retrofit.client.Client;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by drew on 3/9/15.
 */
@Module(
        includes = RetrofitCoreModule.class
)
public class RetrofitModule {

    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s

    @Provides @Singleton
    public Client provideRetrofitClient(SSLSocketFactory sslSocketFactory) {
        final OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setSslSocketFactory(sslSocketFactory);
        return new OkClient(client);
    }

    static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    static final int CORE_POOL_SIZE = 0;// 1;
    static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    @Provides @Singleton
    public Executor provideRetrofitHttpExecutor(ThreadFactory factory) {
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), //new SynchronousQueue<Runnable>(),
                factory
        );
    }

}
