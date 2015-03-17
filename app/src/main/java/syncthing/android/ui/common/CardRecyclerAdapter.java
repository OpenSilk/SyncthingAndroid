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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by drew on 3/10/15.
 */
public abstract class CardRecyclerAdapter extends RecyclerView.Adapter<CardViewHolder> {

    CanExpand.OnExpandListener expandListener;

    public CardRecyclerAdapter() {
    }

    public CanExpand.OnExpandListener getExpandListener() {
        return expandListener;
    }

    public void setExpandListener(CanExpand.OnExpandListener expandListener) {
        this.expandListener = expandListener;
    }

    public abstract Card getItem(int pos);

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(viewType, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        Card c = getItem(position);
        holder.bind(c, expandListener);
    }

    @Override
    public int getItemViewType(int pos) {
        Card c = getItem(pos);
        return c.getLayout();
    }

    @Override
    public void onViewRecycled(CardViewHolder holder) {
        holder.recycle();
    }
}
