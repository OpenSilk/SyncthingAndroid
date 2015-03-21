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

import timber.log.Timber;

/**
 * Created by drew on 3/20/15.
 */
public abstract class ExpandableCardViewWrapper<T extends Card> extends FrameLayout implements BindsCard, CanExpand {

    Card card;
    Expandable expandable;
    CanExpand.OnExpandListener expandListener;

    public ExpandableCardViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected abstract void onBind(T card);

    Expandable getExpandable() {
        return expandable;
    }

    @Override
    public T getCard() {
        return (T) card;
    }

    @Override
    public final void bind(Card card) {
        this.card = card;
        if (!(card instanceof Expandable)) {
            throw new IllegalArgumentException("Bound card not expandable");
        }
        this.expandable = (Expandable) card;
        if (getExpandView() != null) {
            int newVis = this.expandable.isExpanded() ? VISIBLE : GONE;
            if (getExpandView().getVisibility() != newVis) {
                getExpandView().setVisibility(newVis);
            }
        }
        onBind((T) card);
    }

    @Override
    public void reset() {
        card = null;
        expandable = null;
    }

    public CanExpand.OnExpandListener getExpandListener() {
        return expandListener;
    }

    public void setExpandListener(CanExpand.OnExpandListener expandListener) {
        this.expandListener = expandListener;
    }

    public boolean isExpanded() {
        return getExpandable() != null && getExpandable().isExpanded();
    }

    public void setExpanded(boolean expanded) {
        if (getExpandable() != null) {
            getExpandable().setExpanded(expanded);
        }
    }

    public void expand() {
        Timber.d("expand %s",Integer.toHexString(hashCode()));
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
        Timber.d("collapse %s",Integer.toHexString(hashCode()));
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
