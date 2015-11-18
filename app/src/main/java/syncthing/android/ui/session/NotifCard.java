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

import android.support.annotation.NonNull;

import syncthing.android.ui.common.ExpandableCard;

/**
 * Created by drew on 10/12/15.
 */
public abstract class NotifCard extends ExpandableCard implements Comparable<NotifCard> {

    enum Kind {
        RESTART,
        ERROR,
        DEVICE_REJ,
        FOLDER_REJ,
    }

    protected final SessionPresenter presenter;
    protected final Kind kind;

    public NotifCard(SessionPresenter presenter, Kind kind) {
        this.presenter = presenter;
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public int compareTo(@NonNull NotifCard another) {
        return getKind().ordinal() - another.getKind().ordinal();
    }
}
