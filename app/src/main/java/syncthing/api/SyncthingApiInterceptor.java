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

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by drew on 10/10/15.
 */
public class SyncthingApiInterceptor implements Interceptor {
    final SyncthingApiConfig config;

    public SyncthingApiInterceptor(SyncthingApiConfig config) {
        this.config = config;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!StringUtils.isEmpty(config.getApiKey())) {
            request = request.newBuilder()
                    .addHeader(SyncthingApi.HEADER_API_KEY, StringUtils.trim(config.getApiKey()))
                    .build();
        } else if (!StringUtils.isEmpty(config.getAuth())) {
            request = request.newBuilder()
                    .addHeader("Authorization", StringUtils.trim(config.getAuth()))
                    .build();
        }
        if (config.isDebug()) {
            Timber.d("Calling %s", request.urlString());
        }
        return chain.proceed(request);
    }
}
