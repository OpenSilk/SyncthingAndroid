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

import android.databinding.PropertyChangeRegistry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper for recycler items to abstract adapter fuctionality
 *
 * Created by drew on 3/10/15.
 */
public abstract class Card implements android.databinding.Observable {

    private static final AtomicInteger idGenerator = new AtomicInteger(1);
    protected PropertyChangeRegistry mRegistry = new PropertyChangeRegistry();
    private final int adapterId = idGenerator.getAndIncrement();

    public abstract int getLayout();

    public int adapterId() {
        return adapterId;
    }

    public boolean isSame(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (adapterId() != ((Card)o).adapterId()) return false;
        return true;
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        mRegistry.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        mRegistry.remove(callback);
    }

    protected void notifyChange(int val) {
        mRegistry.notifyChange(this, val);
    }

    public boolean canExpand() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return adapterId == card.adapterId;
    }

    @Override
    public int hashCode() {
        return adapterId;
    }
}
