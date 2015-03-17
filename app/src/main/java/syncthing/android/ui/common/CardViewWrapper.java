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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by drew on 3/10/15.
 */
public abstract class CardViewWrapper extends FrameLayout implements BindsCard, CanExpand {

    Card card;
    CanExpand.OnExpandListener expandListener;
    boolean expanded = false;

    public CardViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public abstract void bind(Card card);

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public CanExpand.OnExpandListener getExpandListener() {
        return expandListener;
    }

    public void setExpandListener(CanExpand.OnExpandListener expandListener) {
        this.expandListener = expandListener;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void expand() {
        if (getExpandView() != null) {
            if (expandListener != null) {
                expandListener.onExpandStart(this, getExpandView());
            } else {
                getExpandView().setVisibility(VISIBLE);
                setExpanded(true);
            }
        }
    }

    public void collapse() {
        if (getExpandView() != null) {
            if (expandListener != null) {
                expandListener.onCollapseStart(this, getExpandView());
            } else {
                getExpandView().setVisibility(GONE);
                setExpanded(false);
            }
        }
    }

    public void toggleExpanded() {
        if (isExpanded()) {
            collapse();
        } else {
            expand();
        }
    }

}
