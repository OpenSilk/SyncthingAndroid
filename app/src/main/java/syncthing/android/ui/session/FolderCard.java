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
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ExpandableCard;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;
import syncthing.api.model.Model;
import syncthing.api.model.ModelState;
import syncthing.api.model.PullOrder;
import syncthing.api.model.VersioningType;

/**
 * Created by drew on 3/10/15.
 */
public class FolderCard extends ExpandableCard {

    private final SessionPresenter presenter;
    private FolderConfig folder;
    private Model model;

    public FolderCard(SessionPresenter presenter, FolderConfig folder, Model model) {
        this.presenter = presenter;
        this.folder = folder;
        this.model = model;
    }

    public void setFolder(FolderConfig folder) {
        if (!StringUtils.equals(folder.id, this.folder.id)) {
            throw new IllegalArgumentException("Tried binding different folder to this card " +
                    folder.id + " != " +this.folder.id);
        }
        this.folder = folder;
        notifyChange(syncthing.android.BR._all);//TODO only notify changed fields
    }

    public void setModel(Model model) {
        if (this.model == null || model == null) {
            this.model = model;
            notifyChange(syncthing.android.BR._all);
        } else {
            Model oldModel = this.model;
            this.model = model;
            if (oldModel.state != model.state) {
                notifyChange(syncthing.android.BR.state);
            }
            if (!StringUtils.equals(oldModel.invalid, model.invalid)) {
                notifyChange(syncthing.android.BR.invalid);
            }
            if (oldModel.globalFiles != model.globalFiles) {
                notifyChange(syncthing.android.BR.globalFiles);
            }
            if (oldModel.globalBytes != model.globalBytes) {
                notifyChange(syncthing.android.BR.globalBytes);
                notifyChange(syncthing.android.BR.completion);
            }
            if (oldModel.localFiles != model.localFiles) {
                notifyChange(syncthing.android.BR.localFiles);
            }
            if (oldModel.localBytes != model.localBytes) {
                notifyChange(syncthing.android.BR.localBytes);
            }
            if (oldModel.needFiles != model.needFiles) {
                notifyChange(syncthing.android.BR.needFiles);
            }
            if (oldModel.needBytes != model.needBytes) {
                notifyChange(syncthing.android.BR.needBytes);
            }
            if (oldModel.inSyncBytes != model.inSyncBytes) {
                notifyChange(syncthing.android.BR.completion);
            }
            if (oldModel.ignorePatterns != model.ignorePatterns) {
                notifyChange(syncthing.android.BR.ignorePatterns);
            }
        }
    }

    public void setState(ModelState state) {
        if (model != null && state != null) {
            model.state = state;
            notifyChange(syncthing.android.BR.state);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.session_folder;
    }

    @Bindable
    public String getId(){
        return folder.id;
    }

    @Bindable
    public String getPath() {
        return folder.path;
    }

    @Bindable
    public String getInvalid() {
        return model != null ? model.invalid : folder.invalid;
    }

    @Bindable
    public boolean getReadOnly() {
        return folder.readOnly;
    }

    @Bindable
    public boolean getIgnorePerms() {
        return folder.ignorePerms;
    }

    @Bindable
    public int getRescanIntervalS() {
        return folder.rescanIntervalS;
    }

    @Bindable
    public PullOrder getPullOrder() {
        return folder.order;
    }

    @Bindable
    public VersioningType getVersioningType() {
        return folder.versioning.type;
    }

    @Bindable
    public ModelState getState() {
        return model != null ? model.state : ModelState.UNKNOWN;
    }

    @Bindable
    public long getGlobalFiles() {
        return model != null ? model.globalFiles : 0;
    }

    @Bindable
    public long getGlobalBytes() {
        return model != null ? model.globalBytes : 0;
    }

    @Bindable
    public long getLocalFiles() {
        return model != null ? model.localFiles : 0;
    }

    @Bindable
    public long getLocalBytes() {
        return model != null ? model.localBytes : 0;
    }

    @Bindable
    public long getNeedFiles() {
        return model != null ? model.needFiles : 0;
    }

    @Bindable
    public long getNeedBytes() {
        return model != null ? model.needBytes : 0;
    }

    @Bindable
    public int getCompletion() {
        return model != null ? calculateCompletion(model) : -1;
    }

    @Bindable
    public boolean getIgnorePatterns() {
        return model != null && model.ignorePatterns;
    }

    @Bindable
    public List<FolderDeviceConfig> getDevices() {
        return folder.devices;
    }

    @BindingAdapter("folderSharedWith")
    public static void folderSharedWith(SessionPresenter presenter, TextView view, List<FolderDeviceConfig> devices) {
        List<String> sharedNames = new ArrayList<>();
        for (FolderDeviceConfig d : devices) {
            if (!StringUtils.equals(d.deviceID, presenter.getMyDeviceId())) {
                DeviceConfig dev = presenter.controller.getDevice(d.deviceID);//TODO stop doing this
                if (dev != null) {
                    sharedNames.add(SyncthingUtils.getDisplayName(dev));
                }
            }
        }
        Collections.sort(sharedNames);
        if (sharedNames.isEmpty()) {
            view.setText("");
        } else {
            StringBuilder b = new StringBuilder(sharedNames.get(0));
            for (int ii=1; ii<sharedNames.size(); ii++) {
                b.append(", ").append(sharedNames.get(ii));
            }
            view.setText(b.toString());
        }
    }

    static int calculateCompletion(Model model) {
        return (model.globalBytes != 0)
                ? Math.min(100, Math.round(100f * model.inSyncBytes / model.globalBytes))
                : 100;
    }

    public void overrideFolderChanges(View btn) {
        presenter.overrideChanges(getId());
    }

    public void rescanFolder(View btn) {
        presenter.scanFolder(getId());
    }

    public void editFolder(View btn) {
        presenter.openEditFolderScreen(getId());
    }

}
