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

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by drew on 3/10/15.
 */
public class CardViewHolder extends RecyclerView.ViewHolder {

    public CardViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(Card card, CanExpand.OnExpandListener listener) {
        if (itemView instanceof BindsCard) {
            ((BindsCard) itemView).bind(card);
        }
        if (itemView instanceof CanExpand) {
            CanExpand ce = (CanExpand) itemView;
            ce.setExpandListener(listener);
        }
    }

    public void recycle() {
        if (itemView instanceof BindsCard) {
            ((BindsCard) itemView).reset();
        }
        if (itemView instanceof CanExpand) {
            CanExpand ce = (CanExpand) itemView;
            ce.setExpandListener(null);
        }

    }
}
