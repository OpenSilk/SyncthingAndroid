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

import android.databinding.Bindable;

import org.apache.commons.lang3.StringUtils;

import syncthing.android.R;
import syncthing.api.model.SystemError;

/**
 * Created by drew on 3/15/15.
 */
public class NotifCardError extends NotifCard {

    public static final NotifCardError INSTANCE = new NotifCardError();

    private SystemError guiError;

    private NotifCardError() {
    }

    @Override
    public int getLayout() {
        return R.layout.session_notif_error;
    }

    public void setError(SystemError error) {
        if (error == null) {
            throw new IllegalArgumentException("Tried setting null error");
        } else if (this.guiError == null) {
            this.guiError = error;
            notifyChange(syncthing.android.BR._all);
        } else {
            if (!StringUtils.equals(this.guiError.error, error.error)) {
                this.guiError.error = error.error;
                notifyChange(syncthing.android.BR.message);
            }
            if (!this.guiError.time.equals(error.time)) {
                this.guiError.time = error.time;
                notifyChange(syncthing.android.BR.time);
            }
        }
    }

    @Bindable
    public String getTime() {
        return guiError.time.toString("H:mm:ss");
    }

    @Bindable
    public String getMessage() {
        return guiError.error;
    }
}
