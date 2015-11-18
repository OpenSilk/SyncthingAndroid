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
import android.view.View;

import org.apache.commons.lang3.StringUtils;

import syncthing.android.R;
import syncthing.api.model.SystemMessage;

/**
 * Created by drew on 3/15/15.
 */
public class NotifCardError extends NotifCard {

    private SystemMessage message;

    public NotifCardError(SessionPresenter presenter, SystemMessage message) {
        super(presenter, Kind.ERROR);
        setError(message);
    }

    @Override
    public int getLayout() {
        return R.layout.session_notif_error;
    }

    public void setError(SystemMessage error) {
        if (error == null) {
            throw new IllegalArgumentException("Tried setting null error");
        } else if (this.message == null) {
            this.message = error;
            notifyChange(syncthing.android.BR._all);
        } else {
            if (!StringUtils.equals(this.message.message, error.message)) {
                this.message.message = error.message;
                notifyChange(syncthing.android.BR.message);
            }
            if (!this.message.when.equals(error.when)) {
                this.message.when = error.when;
                notifyChange(syncthing.android.BR.time);
            }
        }
    }

    @Bindable
    public String getTime() {
        return message.when.toString("H:mm:ss");
    }

    @Bindable
    public String getMessage() {
        return message.message;
    }

    public void dismissError(View btn) {
        presenter.controller.clearErrors();
    }
}
