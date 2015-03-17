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

package syncthing.api.model;

/**
* Created by drew on 3/17/15.
*/
public class EventData {
    //STARTING
    public String home;
    //DEVICE_DISCOVERED
    public String[] addrs;
    //DEVICE_DISCOVERED,
    //REMOTE_INDEX_UPDATED
    //FOLDER_REJECTED
    //DEVICE_REJECTED
    public String device;
    //DEVICE_CONNECTED
    public String addr;
    //DEVICE_CONNECTED
    //DEVICE_DISCONNECTED
    public String id;
    //DEVICE_DISCONNECTED
    //ITEM_FINISHED
    //public String error; //TODO Itemfinished is an object
    //REMOTE_INDEX_UPDATED
    //ITEM_STARTED
    //ITEM_FINISHED
    //STATE_CHANGED
    //FOLDER_REJECTED
    public String folder;
    //REMOTE_INDEX_UPDATED
    public long items;
    //LOCAL_INDEX_UPDATED
    public String flags;
    //LOCAL_INDEX_UPDATED
    public String modified;
    //LOCAL_INDEX_UPDATED
    public String name;
    //LOCAL_INDEX_UPDATED
    public long size;
    //ITEM_STARTED
    //ITEM_FINISHED
    public String item;
    //ITEM_STARTED
    /* TODO details {
        int Flags;
        long Version;
        String Name;
        int NumBlocks;
        long LocalVersion;
        long Modified;
    } */
    //STATE_CHANGED
    public ModelState from;
    //STATE_CHANGED
    public ModelState to;
    //STATE_CHANGED
    public float duration;
    //DEVICE_REJECTED
    public String address;
    //TODO CONFIG_SAVED
    //TODO DOWNLOAD_PROGRESS
}
