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

import android.app.AlertDialog;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.opensilk.common.ui.mortar.ActionBarMenuHandler;

import syncthing.android.R;

/**
 * Created by drew on 10/11/15.
 */
public class SessionMenuHandler implements ActionBarMenuHandler {

    final SessionPresenter mPresenter;

    public SessionMenuHandler(SessionPresenter mPresenter) {
        this.mPresenter = mPresenter;
    }

    @Override
    public boolean onBuildMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.session, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClicked(Context context, MenuItem menuItem) {
        if (!mPresenter.controller.isOnline() || mPresenter.controller.getSystemInfo() == null)
            return false;
        switch (menuItem.getItemId()) {
            case R.id.add_device:
                mPresenter.openAddDeviceScreen();
                return true;
            case R.id.add_folder:
                mPresenter.openAddFolderScreen();
                return true;
            case R.id.settings:
                mPresenter.openSettingsScreen();
                return true;
            case R.id.show_id: {
                mPresenter.showIdDialog();
                return true;
            } case R.id.shutdown: {
                //todo this could leak if not dismissed
                new AlertDialog.Builder(context)
                        .setTitle(R.string.shutdown)
                        .setMessage(R.string.are_you_sure_you_want_to_shutdown_syncthing)
                        .setPositiveButton(R.string.shutdown, (dialog, which) -> mPresenter.controller.shutdown())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            } case R.id.restart:
                mPresenter.controller.restart();
                return true;
            default:
                return false;
        }
    }
}
