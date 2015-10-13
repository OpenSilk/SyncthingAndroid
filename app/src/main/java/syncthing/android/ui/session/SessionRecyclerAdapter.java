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

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import rx.functions.Func1;
import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.CardRecyclerAdapter;
import syncthing.android.ui.common.CardViewHolder;
import syncthing.android.ui.common.ExpandableCard;

/**
 * Created by drew on 3/1/15.
 */
public class SessionRecyclerAdapter extends CardRecyclerAdapter {

    final SessionPresenter mPresenter;

    //order here is display order
    List<ExpandableCard> notifications = new LinkedList<>();
    HeaderCard folderHeader = HeaderCard.FOLDER;
    List<FolderCard> folderItems = new LinkedList<>();
    HeaderCard deviceHeader = HeaderCard.DEVICE;
    MyDeviceCard thisDevice;
    List<DeviceCard> deviceItems = new LinkedList<>();

    public SessionRecyclerAdapter(SessionPresenter presenter) {
        setHasStableIds(true);
        mPresenter = presenter;
    }

    //for tests
    SessionRecyclerAdapter() {
        this(null);
    }

    @Override
    public android.databinding.DataBindingComponent getBindingComponent() {
        return mPresenter;
    }

    @Override
    public void applyAdditionalBinding(Card card, CardViewHolder holder) {
//        holder.getBinding().setVariable(syncthing.android.BR.clickHandler, this);
    }

    public void setNotifications(List<ExpandableCard> notifs, boolean notify) {
        updateList(notifications, notifs, this::findNotificationOffset, notify);
    }

    public void setFolders(List<FolderCard> folders, boolean notify) {
        updateList(folderItems, folders, this::findFolderOffset, notify);
    }

    public void setThisDevice(MyDeviceCard myDevice, boolean notify) {
        boolean wasnull = thisDevice == null;
        thisDevice = myDevice;
        if (notify) {
            if (wasnull) {
                notifyItemInserted(findThisDevicePos());
            } else {
                notifyItemChanged(findThisDevicePos());
            }
        }
    }

    public void setDevices(List<DeviceCard> devices, boolean notify) {
        updateList(deviceItems, devices, this::findDeviceOffset, notify);
    }

    <T extends ExpandableCard> void  updateList(List<T> lst1, List<T> lst2, Func1<Integer, Integer> findFunc, boolean notify) {
        if (lst1.isEmpty() && !lst2.isEmpty()) {
            lst1.addAll(lst2);
            if (notify) notifyItemRangeInserted(findFunc.call(0), lst2.size());
        } else {
            int oldsize = lst1.size();
            int newsize = lst2.size();
            lst1.clear();
            lst1.addAll(lst2);
            if (notify) {
                if (oldsize == newsize) {
                    notifyItemRangeChanged(findFunc.call(0), oldsize);
                } else if (oldsize < newsize) {
                    notifyItemRangeChanged(findFunc.call(0), oldsize);
                    notifyItemRangeInserted(findFunc.call(oldsize), newsize - oldsize);
                } else {
                    notifyItemRangeChanged(findFunc.call(0), newsize);
                    notifyItemRangeRemoved(findFunc.call(newsize), oldsize - newsize);
                }
            }
        }
    }

    /*
     * finders locate relative adapter positon of items specified list
     */

    int findNotificationOffset(int index) {
        return index;
    }

    int findFolderOffset(int index) {
        index = findNotificationOffset(index);
        if (!notifications.isEmpty()) {
            index += notifications.size();
        }
        if (folderHeader != null) {
            index++;
        }
        return index;
    }

    int findThisDevicePos() {
        return notifications.size()
                + ((folderHeader != null) ? 1 : 0)
                + folderItems.size()
                + ((deviceHeader != null) ? 1 : 0)
                ;
    }

    int findDeviceOffset(int index) {
        index = findFolderOffset(index);
        if (!folderItems.isEmpty()) {
            index += folderItems.size();
        }
        if (deviceHeader != null) {
            index++;
        }
        if (thisDevice != null) {
            index++;
        }
        return index;
    }

    public Card getItem(int pos) {
        final int origpos = pos;
        if (pos < notifications.size()) {
            return notifications.get(pos);
        }
        pos -= notifications.size();
        if (folderHeader != null) {
            if (pos == 0) {
                return folderHeader;
            }
            pos--;
        }
        if (pos < folderItems.size()) {
            return folderItems.get(pos);
        }
        pos -= folderItems.size();
        if (deviceHeader != null) {
            if (pos == 0) {
                return deviceHeader;
            }
            pos--;
        }
        if (thisDevice != null) {
            if (pos == 0) {
                return thisDevice;
            }
            pos--;
        }
        if (pos < deviceItems.size()) {
            return deviceItems.get(pos);
        }
        throw new IllegalArgumentException(dump() + ", pos " + pos + ", orig "+origpos);
    }

    @Override
    public int getItemCount() {
        return notifications.size()
                + ((folderHeader != null) ? 1 : 0)
                + folderItems.size()
                + ((deviceHeader != null) ? 1 : 0)
                + ((thisDevice != null) ? 1 : 0)
                + deviceItems.size()
                ;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).adapterId();
    }

    String dump() {
        return ReflectionToStringBuilder.reflectionToString(this, RecursiveToStringStyle.MULTI_LINE_STYLE);
    }
}
