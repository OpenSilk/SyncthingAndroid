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

/**
 * Created by drew on 3/12/15.
 */
public interface ActivityRequestCodes {
    int _BASE = 200;
    int LOGIN_ACTIVITY = _BASE << 1;
    int SCAN_QR = _BASE << 2;
    int DIRECTORY_PICKER = _BASE << 3;
    int IMPORT_CONFIG = _BASE << 4;
    int MANAGE_ACTIVITY = _BASE << 5;
    int WELCOME_ACTIVITY = _BASE << 6;
}
