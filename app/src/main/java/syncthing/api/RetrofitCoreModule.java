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

import java.util.concurrent.ThreadFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by drew on 4/11/15.
 */
@Module(
        includes = SSLSocketFactoryModule.class
)
public class RetrofitCoreModule {
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
}
