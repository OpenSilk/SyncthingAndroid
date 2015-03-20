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
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by drew on 3/9/15.
 */
@Module
public class RetrofitModule {

    @Provides @Singleton
    public Converter provideRetrofitConverter(Gson gson) {
        return new GsonConverter(gson);
    }

    @Provides @Singleton
    public ThreadFactory provideretrofitThreadFactory() {
        return r -> new Thread(new Runnable() {
            @Override public void run() {
                android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
                r.run();
            }
        }, "RetrofitAndroid-Idle");
    }

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

    static final int LONGPOLL_CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int LONGPOLL_READ_TIMEOUT_MILLIS = 3 * 60 * 1000; // 3m //20 * 1000; // 20s
    static final int LONGPOLL_MAX_IDLE_CONNECTIONS = 2;
    static final long LONGPOLL_KEEP_ALIVE_DURATION_MS = 5 * 60 * 1000; // 5 min

    @Provides @Singleton @Named("longpoll")
    public Client provideLongpollRetrofitClient(SSLSocketFactory sslSocketFactory) {
        final OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(LONGPOLL_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(LONGPOLL_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setConnectionPool(new ConnectionPool(LONGPOLL_MAX_IDLE_CONNECTIONS, LONGPOLL_KEEP_ALIVE_DURATION_MS));
        client.setSslSocketFactory(sslSocketFactory);
        return new OkClient(client);
    }

    static final int LONGPOLL_CORE_POOL_SIZE = 0;
    static final int LONGPOLL_MAXIMUM_POOL_SIZE = 2;

    //Gross hack, when switching sessions the longpoll will block until it
    //receives an event or it times out which can be minutes, so just make
    //a new executor until i figure a better way
    @Provides /*@Singleton*/ @Named("longpoll")
    public Executor provideLongpollRetrofitHttpExecutor(ThreadFactory factory) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                LONGPOLL_CORE_POOL_SIZE, LONGPOLL_MAXIMUM_POOL_SIZE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                factory
        );
        return executor;
    }

    @Provides @Singleton
    SSLSocketFactory provideStupidSSLSocketFactory() {
        final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
