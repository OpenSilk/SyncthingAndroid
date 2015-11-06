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
 * Describes a view that can be expanded
 *
 * Created by drew on 3/10/15.
 */
public interface CanExpand {
    /**
     * Interface to listen any callbacks when expand/collapse animation starts
     * Listeners must notify the Expandable after expand/collapse completes
     */
    interface OnExpandListener {
        void onExpandStart(CanExpand viewCard, View expandingLayout);
        void onCollapseStart(CanExpand viewCard, View expandingLayout);
    }
    void setExpandable(Expandable card);
    Expandable getExpandable();
    void setExpandListener(OnExpandListener listener);
    OnExpandListener getExpandListener();
    void expand();
    void collapse();
    void toggleExpanded();
    View getView();
}
