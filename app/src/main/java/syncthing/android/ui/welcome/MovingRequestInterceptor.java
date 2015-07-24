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

package syncthing.android.ui.welcome;

import javax.inject.Inject;

import retrofit.RequestInterceptor;
import syncthing.api.SyncthingApi;

/**
 * Created by drew on 3/12/15.
 */
@WelcomeScreenScope
public class MovingRequestInterceptor implements RequestInterceptor {

    String authorization;
    String apiKey;

    @Inject
    public MovingRequestInterceptor() {
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Authorization", authorization);
        request.addHeader(SyncthingApi.HEADER_API_KEY, apiKey);
    }

}
