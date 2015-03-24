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

package syncthing.android.ui.session.edit;

import android.view.View;

import mortar.ViewPresenter;
import rx.Subscription;
import syncthing.android.ui.session.SessionPresenter;
import syncthing.api.SessionController;

/**
 * Created by drew on 3/23/15.
 */
public class EditPresenter<V extends View> extends ViewPresenter<V> {

    protected final SessionController controller;
    protected final EditFragmentPresenter editFragmentPresenter;
    protected final SessionPresenter sessionPresenter;
    protected final String folderId;
    protected final String deviceId;
    protected final boolean isAdd;

    protected Subscription saveSubscription;

    public EditPresenter(
            SessionController controller,
            EditFragmentPresenter editFragmentPresenter,
            SessionPresenter sessionPresenter,
            String folderId,
            String deviceId,
            boolean isAdd
    ) {
        this.controller = controller;
        this.editFragmentPresenter = editFragmentPresenter;
        this.sessionPresenter = sessionPresenter;
        this.folderId = folderId;
        this.deviceId = deviceId;
        this.isAdd = isAdd;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        if (saveSubscription != null) {
            saveSubscription.unsubscribe();
        }
    }

    protected  void dismissDialog() {
        editFragmentPresenter.dismissDialog();
    }
}
