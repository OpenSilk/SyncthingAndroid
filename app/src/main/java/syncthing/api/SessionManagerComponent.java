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

import org.opensilk.common.core.dagger2.AppContextComponent;

import java.util.concurrent.Executor;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import retrofit.client.Client;
import retrofit.converter.Converter;
import syncthing.android.AppModule;
import syncthing.android.identicon.IdenticonModule;
import syncthing.android.model.Credentials;

/**
 * Created by drew on 10/10/15.
 */
public interface SessionManagerComponent {
    Gson gson();
    Converter retrofitConverter();
    Client retrofitClient();
    Executor retrofitHttpExecutor();
    @Named("longpoll") Client longpollRetrofitClient();
    @Named("longpoll") Executor longpollretrofitHttpExecutor();
    SessionManager sessionManager();
    SessionComponent newSession(SessionModule module, SyncthingApiModule apiModule,
                                SyncthingApiLongpollModule longpollModule);
}
