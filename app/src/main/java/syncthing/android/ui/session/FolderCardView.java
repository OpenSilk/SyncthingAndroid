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
    @InjectView(R.id.versioning_container) ViewGroup versioningHider;
    @InjectView(R.id.versioning) TextView versioning;
    @InjectView(R.id.shared_with) TextView sharedWith;

    @InjectView(R.id.btn_override) Button btnOverride;
    @InjectView(R.id.btn_rescan) Button btnRescan;

    final SessionPresenter presenter;

    Subscription modelSubscription;
    Subscription modelStateSubscription;

    public FolderCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<SessionComponent>getDaggerComponent(getContext()).presenter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unsubscribe();
    }

    @OnClick(R.id.header)
    void toggleExpand() {
        toggleExpanded();
    }

    @OnClick(R.id.btn_override)
    void overrideChanges() {
        if (getCard() == null) return;
        presenter.overrideChanges(getCard().folder.id);
    }

    @OnClick(R.id.btn_rescan)
    void rescanFolder() {
        if (getCard() == null) return;
        presenter.scanFolder(getCard().folder.id);
    }

    @OnClick(R.id.btn_edit)
    void addFolder() {
        if (getCard() == null) return;
        presenter.openEditFolderScreen(getCard().folder.id);
    }

    void openFolder() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(getCard().folder.path);
        intent.setDataAndType(uri, "*/*");
        getContext().startActivity(intent);
    }

    @Override
    public View getExpandView() {
        return expand;
    }

    @Override
    public void onBind(FolderCard card) {
        updateFolder(card.folder);
        updateModel(card.model);
        subscribe();
    }

    @Override
    public void reset() {
        super.reset();
        unsubscribe();
    }

    void updateFolder(FolderConfig folder) {

        id.setText(folder.id);
        directory.setText((folder.path));
        error.setText(folder.invalid);
        errorHider.setVisibility(StringUtils.isEmpty(folder.invalid) ? GONE : VISIBLE);
        folderMasterHider.setVisibility(folder.readOnly ? VISIBLE : GONE);
        ignorePermsHider.setVisibility(folder.ignorePerms ? VISIBLE : GONE);
        rescanInterval.setText(String.valueOf(folder.rescanIntervalS));

        if (folder.versioning.type != VersioningType.NONE) {
            versioningHider.setVisibility(VISIBLE);
            versioning.setText(folder.versioning.type.localizedString(getContext()));
        } else {
            versioningHider.setVisibility(GONE);
        }

        List<String> sharedNames = new ArrayList<>();
        for (FolderDeviceConfig d : folder.devices) {
            if (!StringUtils.equals(d.deviceID, presenter.getMyDeviceId())) {
                DeviceConfig dev = presenter.controller.getDevice(d.deviceID);//TODO stop doing this
                if (dev != null) {
                    sharedNames.add(SyncthingUtils.getDisplayName(dev));
                }
            }
        }
        Collections.sort(sharedNames);
        if (sharedNames.isEmpty()) {
            sharedWith.setText("");
        } else {
            StringBuilder b = new StringBuilder();
            for (int ii=0; ii<sharedNames.size(); ii++) {
                b.append(sharedNames.get(ii));
                if (ii+1 < sharedNames.size()) {
                    b.append(", ");
                }
            }
            sharedWith.setText(b.toString());
        }

    }

    void updateModel(Model model) {
        if (model == null) {
            updateFolderStatus(null, 0);
            return;
        }

        updateFolderStatus(model.state, calculateCompletion(model));

        globalState.setText(getContext().getString(R.string.num_items_size,
                        model.globalFiles,
                        SyncthingUtils.humanReadableSize(model.globalBytes))
        );

        localState.setText(getContext().getString(R.string.num_items_size,
                        model.localFiles,
                        SyncthingUtils.humanReadableSize(model.localBytes))
        );

        if (model.needFiles > 0) {
            needFilesHider.setVisibility(VISIBLE);
            needFiles.setText(getContext().getString(R.string.num_items_size,
                            model.needFiles,
                            SyncthingUtils.humanReadableSize(model.needBytes))
            );
        } else {
            needFilesHider.setVisibility(GONE);
        }

        ignorePatternsHider.setVisibility(model.ignorePatterns ? VISIBLE : GONE);

        error.setText(model.invalid);
        errorHider.setVisibility(StringUtils.isEmpty(model.invalid) ? GONE : VISIBLE);
    }

    void updateFolderStatus(ModelState status, int completion) {

        if (status == null) {
            state.setText(R.string.unknown);
            state.setTextColor(getResources().getColor(R.color.folder_default));
            btnRescan.setVisibility(VISIBLE);
            btnOverride.setVisibility(getCard().folder.readOnly ? VISIBLE : GONE);
            return;
        }

        btnRescan.setVisibility((status == ModelState.SCANNING) ? GONE : VISIBLE);
        btnOverride.setVisibility((status == ModelState.SCANNING) ? GONE
                : (getCard().folder.readOnly ? VISIBLE : GONE));

        state.setText(getContext().getString(R.string.status_percent_complete,
                        status.localizedString(getContext()), completion)
        );

        Resources res = getContext().getResources();
        switch (status) {
            case IDLE:
                state.setTextColor(res.getColor(R.color.folder_idle));
                break;
            case SCANNING:
            case SYNCING:
                state.setTextColor(res.getColor(R.color.folder_scanning));
                break;
            default:
                state.setTextColor(res.getColor(R.color.folder_default));
        }
    }

    void subscribe() {
        unsubscribe();
        modelSubscription = presenter.bus.subscribe(
                m -> {
                    if (!StringUtils.equals(m.id, getCard().folder.id)) {
                        return;
                    }
                    Timber.d("Update.Model(%s)", getCard().folder.id);
                    getCard().setModel(m.model);
                    updateModel(m.model);
                },
                Update.Model.class
        );
        modelStateSubscription = presenter.bus.subscribe(
                m -> {
                    if (!StringUtils.equals(m.id, getCard().folder.id)) {
                        return;
                    }
                    Timber.d("Update.ModelState(%s)", getCard().folder.id);
                    getCard().setModel(m.model);
                    updateFolderStatus(m.model.state, calculateCompletion(m.model));
                },
                Update.ModelState.class
        );
    }

    void unsubscribe() {
        if (modelSubscription != null) {
            modelSubscription.unsubscribe();
        }
        if (modelStateSubscription != null) {
            modelStateSubscription.unsubscribe();
        }
    }

    static int calculateCompletion(Model model) {
        return (model.globalBytes != 0)
                ? Math.min(100, Math.round(100f * model.inSyncBytes / model.globalBytes))
                : 100;
    }

}
