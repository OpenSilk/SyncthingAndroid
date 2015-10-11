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

import syncthing.android.model.Credentials;
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

    public Session acquire(Credentials credentials) {
        for (Session session : sessions) {
            if (session.credentials().equals(credentials)) {
                session.refs.incrementAndGet();
                return session;
            }
        }
        final SessionComponent component = parent.newSession(
                new SessionModule(new SyncthingApiConfig(credentials))
        );
        final Session session = new Session(credentials, component);
        sessions.add(session);
        return session;
    }

    public void release(@NonNull Session session) {
        if (session.refs.decrementAndGet() == 0) {
            Timber.d("Destroying session %s", session.credentials());
            sessions.remove(session);
            session.destroy();
        }
    }

}
