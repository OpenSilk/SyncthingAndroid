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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import timber.log.Timber;

/**
 * Created by drew on 3/20/15.
 */
public class ExpandableCardViewWrapper extends FrameLayout implements CanExpand {

    protected Expandable expandable;
    protected CanExpand.OnExpandListener expandListener;
    protected View expandView;

    public ExpandableCardViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setExpandable(@NonNull Expandable card) {
        expandable = card;
        expandView = findViewById(expandable.getExpandableViewId());
        if (expandView == null) {
            throw new NullPointerException("Unable to find expand view in layout");
        }
        int newVis = expandable.isExpanded() ? VISIBLE : GONE;
        if (expandView.getVisibility() != newVis) {
            expandView.setVisibility(newVis);
        }
    }

    @Override
    public Expandable getExpandable() {
        return expandable;
    }

    @Override
    public @Nullable CanExpand.OnExpandListener getExpandListener() {
        return expandListener;
    }

    @Override
    public void setExpandListener(CanExpand.OnExpandListener expandListener) {
        this.expandListener = expandListener;
    }

    @Override
    public void expand() {
        Timber.d("expand %s",Integer.toHexString(hashCode()));
        if (expandListener != null) {
            expandListener.onExpandStart(this, expandView);
        } else {
            expandView.setVisibility(VISIBLE);
            expandable.setExpanded(true);
        }
    }

    @Override
    public void collapse() {
        Timber.d("collapse %s",Integer.toHexString(hashCode()));
        if (expandListener != null) {
            expandListener.onCollapseStart(this, expandView);
        } else {
            expandView.setVisibility(GONE);
            expandable.setExpanded(false);
        }
    }

    @Override
    public void toggleExpanded() {
        if (expandable.isExpanded()) {
            collapse();
        } else {
            expand();
        }
    }

    @Override
    public View getView() {
        return this;
    }
}
