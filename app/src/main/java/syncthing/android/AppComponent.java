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

package syncthing.android;

import android.content.Context;

import com.google.gson.Gson;

import org.opensilk.common.dagger2.ForApplication;

import java.util.concurrent.Executor;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import retrofit.client.Client;
import retrofit.converter.Converter;
import syncthing.android.identicon.IdenticonGenerator;
import syncthing.android.identicon.IdenticonModule;
import syncthing.api.GsonModule;
import syncthing.api.RetrofitModule;

/**
 * Created by drew on 3/4/15.
 */
@Singleton
@Component (
        modules = {
                AppModule.class,
                GsonModule.class,
                RetrofitModule.class,
                IdenticonModule.class
        }
)
public interface AppComponent {
    String NAME = AppComponent.class.getName();
    @ForApplication Context appContext();
    Gson gson();
    Converter retrofitConverter();
    Client retrofitClient();
    Executor retrofitHttpExecutor();
    IdenticonGenerator identiconGenerator();
    @Named("longpoll") Client longpollRetrofitClient();
    @Named("longpoll") Executor longpollretrofitHttpExecutor();
    AppSettings appSettings();
}
