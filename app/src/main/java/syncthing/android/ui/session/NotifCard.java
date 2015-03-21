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

package syncthing.android.ui.session;

import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.ExpandableCard;
import syncthing.api.model.Event;

/**
 * Created by drew on 3/10/15.
 */
public abstract class NotifCard extends ExpandableCard {

    final String id;
    final Event event;

    public NotifCard(String id, Event event) {
        this.id = id;
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotifCard that = (NotifCard) o;

        if (!id.equals(that.id))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 31 * event.time.toString().hashCode() + id.hashCode();
    }

}
