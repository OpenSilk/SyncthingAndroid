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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mortar.dagger2support.DaggerService;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.CardViewWrapper;
import syncthing.api.model.DeviceConfig;

/**
 * Created by drew on 3/6/15.
 */
public class NotifCardFolderRejView extends CardViewWrapper {

    @InjectView(R.id.header) ViewGroup header;
    @InjectView(R.id.expand) ViewGroup expand;
    @InjectView(R.id.title) TextView title;
    @InjectView(R.id.time) TextView time;
    @InjectView(R.id.message) TextView message;
    @InjectView(R.id.btn_add) Button btnAdd;

    final SessionPresenter presenter;

    NotifCardFolderRej item;
    boolean share;

    public NotifCardFolderRejView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<SessionComponent>getDaggerComponent(getContext()).presenter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @OnClick(R.id.header)
    void doExpandThing() {
        toggleExpanded();
    }

    @OnClick(R.id.btn_add)
    void addFolder() {
        if (share) {
            presenter.showSavingDialog();
            //TODO fix
            presenter.controller.shareFolder(item.event.data.folder, item.event.data.device,
                    t -> {
                        presenter.showError("Share failed", t.getMessage());
                    },
                    () -> {
                        presenter.dismissSavingDialog();
                        presenter.showSuccessMsg();
                        dismissFolder();
                    }
            );
        } else {
            dismissFolder();
            presenter.openEditFolderScreen(item.event.data.folder, item.event.data.device);
        }
    }

    @OnClick(R.id.btn_later)
    void dismissFolder() {
        presenter.controller.removeFolderRejection(item.id);
    }

    public void bind(Card card) {
        item = (NotifCardFolderRej) card;
        share = presenter.controller.getFolder(item.event.data.folder) != null;
        if (share) {
            title.setText(R.string.share_this_folder);
            btnAdd.setText(R.string.share);
        } else {
            title.setText(R.string.add_new_folder);
            btnAdd.setText(R.string.add);
        }
        time.setText(item.event.time.toString("H:mm:ss"));
        DeviceConfig device = presenter.controller.getDevice(item.event.data.device);
        if (device == null) {
            device = new DeviceConfig();
            device.deviceID = item.event.data.device;
        }
        String name = SyncthingUtils.getDisplayName(device);
        message.setText(getResources().getString(
                R.string.device_wants_to_share_folder_folder,
                name,
                item.event.data.folder
        ));
    }

    @Override
    public View getExpandView() {
        return expand;
    }

}
