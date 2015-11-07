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

package syncthing.android.ui.common;

import android.view.View;

/**
 * Created by drew on 10/13/15.
 */
public class ExpandHandler {
    final ExpandableView cardView;

    public static ExpandHandler create(Expandable card, ExpandableView cardView, ExpandableView.OnExpandListener listener) {
        cardView.setExpandable(card);
        cardView.setExpandListener(listener);
        return new ExpandHandler(cardView);
    }

    private ExpandHandler(ExpandableView cardView) {
        this.cardView = cardView;
    }

    public void onClick(View view) {
        cardView.toggleExpanded();
    }
}
