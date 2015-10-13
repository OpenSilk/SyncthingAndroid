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

import android.view.View;

/**
 * Created by drew on 10/13/15.
 */
public interface SessionItemClickHandler {
    //Device card
    void editDevice(View view, String device);
    //Folder card
    void overrideChanges(String folder);
    void rescanFolder(String folder);
    void editFolder(String folder);
    //notif error
    void dismissErrors();
    //notif device rej
    void addDevice(String device);
    void ignoreDevice(String device);
    void dismissDevice(String device);
    //notif folder rej
    void addFolder(String folder, String device);
    void dismissFolder(String folder);
    //notif restart
    void restartSyncthing();
}
