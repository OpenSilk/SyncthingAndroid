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
 * TODO this is atrocious, could maybe just do a map, and cast things in SessionController
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
    //FOLDER_COMPLETION
    public String device;
    //DEVICE_CONNECTED
    public String addr;
    //DEVICE_CONNECTED
    //DEVICE_DISCONNECTED
    public String id;
    //DEVICE_DISCONNECTED
    //ITEM_FINISHED
    //public String error; //TODO Itemfinished is an object
    //LOCAL_INDEX_UPDATED
    //REMOTE_INDEX_UPDATED
    //ITEM_STARTED
    //ITEM_FINISHED
    //STATE_CHANGED
    //FOLDER_REJECTED
    //FOLDER_SUMMARY
    //FOLDER_COMPLETION
    public String folder;
    //REMOTE_INDEX_UPDATED
    public int items;
    //REMOTE_INDEX_UPDATED
    public long version;
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
    //FOLDER_SUMMARY
    public Model summary;
    //FOLDER_COMPLETION
    public float completion;
}
