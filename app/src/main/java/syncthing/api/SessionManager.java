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

import android.content.Context;
import android.support.annotation.NonNull;

import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.mortar.DaggerService;

import java.util.LinkedHashSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by drew on 10/10/15.
 */
@Singleton
public class SessionManager {

    private final SessionManagerComponent parent;

    @Inject
    public SessionManager(@ForApplication Context appContext) {
        parent = DaggerService.getDaggerComponent(appContext);
    }

    final LinkedHashSet<Session> sessions = new LinkedHashSet<>();

    public Session acquire(@NonNull Credentials credentials) {
        for (Session session : sessions) {
            if (credentials.equals(session.extras().getParcelable(Session.EXTRA_CREDENTIALS))) {
                session.refs.incrementAndGet();
                return session;
            }
        }
        final SyncthingApiConfig config = SyncthingApiConfig.builder()
                .forCredentials(credentials).build();
        final SessionComponent component = parent.newSession(
                new SessionModule(config)
        );
        final Session session = new Session(config, component);
        session.extras().putParcelable(Session.EXTRA_CREDENTIALS, credentials);
        sessions.add(session);
        return session;
    }

    /**
     * Get a one off session that will be destroyed as soon as you release it
     * Use for login
     */
    public Session acquire(@NonNull SyncthingApiConfig config) {
        final SessionComponent component = parent.newSession(
                new SessionModule(config)
        );
        final Session session = new Session(config, component);
//        sessions.add(session);
        return session;
    }

    public void release(@NonNull Session session) {
        if (session.refs.decrementAndGet() == 0) {
            Timber.d("Destroying session %s", session.config().url);
            sessions.remove(session);
            session.destroy();
        }
    }

}
