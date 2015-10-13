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

import org.opensilk.common.core.mortar.DaggerService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ExpandableCardViewWrapper;
import syncthing.api.model.DeviceConfig;

/**
 * Created by drew on 3/6/15.
 */
public class NotifCardRejFolderView extends ExpandableCardViewWrapper<NotifCardRejFolder> {

    @InjectView(R.id.header) ViewGroup header;
    @InjectView(R.id.expand) ViewGroup expand;
    @InjectView(R.id.title) TextView title;
    @InjectView(R.id.time) TextView time;
    @InjectView(R.id.message) TextView message;
    @InjectView(R.id.btn_add) Button btnAdd;

    @Inject SessionPresenter mPresenter;

    boolean share;

    public NotifCardRejFolderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            SessionComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
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
            mPresenter.showSavingDialog();
            //TODO fix
            mPresenter.controller.shareFolder(getCard().event.data.folder, getCard().event.data.device,
                    t -> {
                        mPresenter.showError("Share failed", t.getMessage());
                    },
                    () -> {
                        mPresenter.dismissSavingDialog();
                        mPresenter.showSuccessMsg();
                        dismissFolder();
                    }
            );
        } else {
            dismissFolder();
            mPresenter.openEditFolderScreen(getCard().event.data.folder, getCard().event.data.device);
        }
    }

    @OnClick(R.id.btn_later)
    void dismissFolder() {
        mPresenter.controller.removeFolderRejection(getCard().id);
    }

    public void onBind(NotifCardRejFolder card) {
        share = mPresenter.controller.getFolder(card.event.data.folder) != null;
        if (share) {
            title.setText(R.string.share_this_folder);
            btnAdd.setText(R.string.share);
        } else {
            title.setText(R.string.add_new_folder);
            btnAdd.setText(R.string.add);
        }
        DeviceConfig device = mPresenter.controller.getDevice(card.event.data.device);
        if (device == null) {
            device = new DeviceConfig();
            device.deviceID = card.event.data.device;
        }
        String name = SyncthingUtils.getDisplayName(device);
        message.setText(getResources().getString(
                R.string.device_wants_to_share_folder_folder,
                name,
                card.event.data.folder
        ));
    }

    @Override
    public View getExpandView() {
        return expand;
    }

}
