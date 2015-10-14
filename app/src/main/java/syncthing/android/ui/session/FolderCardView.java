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
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Subscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ExpandableCardViewWrapper;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import syncthing.api.model.Model;
import syncthing.api.model.ModelState;
import syncthing.api.model.PullOrder;
import syncthing.api.model.VersioningType;
import timber.log.Timber;

/**
 * Created by drew on 3/1/15.
 */
public class FolderCardView extends ExpandableCardViewWrapper<FolderCard> {

    @InjectView(R.id.id) TextView id;
    @InjectView(R.id.state) TextView state;
    @InjectView(R.id.expand) ViewGroup expand;
    @InjectView(R.id.directory) TextView directory;
    @InjectView(R.id.error_container) ViewGroup errorHider;
    @InjectView(R.id.error) TextView error;
    @InjectView(R.id.global_state) TextView globalState;
    @InjectView(R.id.local_state) TextView localState;
    @InjectView(R.id.need_files_container) ViewGroup needFilesHider;
    @InjectView(R.id.need_files) TextView needFiles;
    @InjectView(R.id.folder_master) ViewGroup folderMasterHider;
    @InjectView(R.id.ignore_patterns) ViewGroup ignorePatternsHider;
    @InjectView(R.id.ignore_perms) ViewGroup ignorePermsHider;
    @InjectView(R.id.rescan_interval) TextView rescanInterval;
    @InjectView(R.id.pull_order_container) ViewGroup pullOrderHider;
    @InjectView(R.id.pull_order) TextView pullOrder;
    @InjectView(R.id.versioning_container) ViewGroup versioningHider;
    @InjectView(R.id.versioning) TextView versioning;
    @InjectView(R.id.shared_with) TextView sharedWith;

    @InjectView(R.id.btn_override) Button btnOverride;
    @InjectView(R.id.btn_rescan) Button btnRescan;

    @Inject SessionPresenter mPresenter;

    public FolderCardView(Context context, AttributeSet attrs) {
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

    @OnClick(R.id.btn_override)
    void overrideChanges() {
        if (getCard() == null) return;
        mPresenter.overrideChanges(getCard().getId());
    }

    @OnClick(R.id.btn_rescan)
    void rescanFolder() {
        if (getCard() == null) return;
        mPresenter.scanFolder(getCard().getId());
    }

    @OnClick(R.id.btn_edit)
    void addFolder() {
        if (getCard() == null) return;
        mPresenter.openEditFolderScreen(getCard().getId());
    }

    void openFolder() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(getCard().getId());
        intent.setDataAndType(uri, "*/*");
        getContext().startActivity(intent);
    }

    @Override
    public View getExpandView() {
        return expand;
    }

    @Override
    public void onBind(FolderCard card) {
    }

}
