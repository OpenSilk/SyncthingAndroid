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
import syncthing.api.model.FolderStats;
import syncthing.api.model.LastFile;
import syncthing.api.model.Model;
import syncthing.api.model.ModelState;
import syncthing.api.model.PullOrder;
import syncthing.api.model.VersioningType;
import syncthing.api.model.event.FolderErrors;
import syncthing.api.model.event.FolderScanProgress;

/**
 * Created by drew on 3/10/15.
 */
public class FolderCard extends ExpandableCard {

    private final SessionPresenter presenter;
    private FolderConfig folder;
    private FolderScanProgress.Data scanProgress;
    private List<FolderErrors.Error> errors;
    private FolderStats stats;

    // model
    private ModelState state = ModelState.UNKNOWN;
    private String invalid;
    private long globalFiles;
    private long globalBytes;
    private long localFiles;
    private long localBytes;
    private long needFiles;
    private long needBytes;
    private long inSyncBytes;
    private boolean ignorePatterns;

    public FolderCard(SessionPresenter presenter, FolderConfig folder, Model model) {
        this.presenter = presenter;
        this.folder = folder;
        setModel(model);
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
        if (model == null) {
            state = ModelState.UNKNOWN;
            notifyChange(syncthing.android.BR._all);
        } else {
            if (state != model.state) {
                state = model.state;
                notifyChange(syncthing.android.BR.state);
            }
            if (!StringUtils.equals(invalid, model.invalid)) {
                invalid = model.invalid;
                notifyChange(syncthing.android.BR.invalid);
            }
            if (globalFiles != model.globalFiles) {
                globalFiles = model.globalFiles;
                notifyChange(syncthing.android.BR.globalFiles);
            }
            if (globalBytes != model.globalBytes) {
                globalBytes = model.globalBytes;
                notifyChange(syncthing.android.BR.globalBytes);
                notifyChange(syncthing.android.BR.completion);
            }
            if (localFiles != model.localFiles) {
                localFiles = model.localFiles;
                notifyChange(syncthing.android.BR.localFiles);
            }
            if (localBytes != model.localBytes) {
                localBytes = model.localBytes;
                notifyChange(syncthing.android.BR.localBytes);
            }
            if (needFiles != model.needFiles) {
                needFiles = model.needFiles;
                notifyChange(syncthing.android.BR.needFiles);
            }
            if (needBytes != model.needBytes) {
                needBytes = model.needBytes;
                notifyChange(syncthing.android.BR.needBytes);
            }
            if (inSyncBytes != model.inSyncBytes) {
                inSyncBytes = model.inSyncBytes;
                notifyChange(syncthing.android.BR.completion);
            }
            if (ignorePatterns != model.ignorePatterns) {
                ignorePatterns = model.ignorePatterns;
                notifyChange(syncthing.android.BR.ignorePatterns);
            }
        }
    }

    public void setState(ModelState state) {
        if (state != null) {
            ModelState oldState = this.state;
            this.state = state;
            notifyChange(syncthing.android.BR.state);
            if (oldState != ModelState.SCANNING && state == ModelState.SCANNING) {
                //reset scan progress on new scan
                setScanProgress(null);
            }
        } else {
            this.state = ModelState.UNKNOWN;
        }
    }

    public void setScanProgress(FolderScanProgress.Data progress) {
        boolean notify = true;
        if (scanProgress != null && progress != null &&
                scanProgress.current == progress.current &&
                scanProgress.total == progress.total) {
            notify = false;
        }
        scanProgress = progress;
        if (notify) {
            notifyChange(syncthing.android.BR.completion);
        }
    }

    public void setErrors(List<FolderErrors.Error> errors) {
        this.errors = errors;
        notifyChange(syncthing.android.BR.numFolderErrors);
    }

    public void setStats(FolderStats stats) {
        boolean notify = false;
        if (this.stats != null && stats != null) {
            if (!this.stats.lastFile.at.equals(stats.lastFile.at)) {
                notify = true;
            }
        }
        this.stats = stats;
        if (notify) {
            notifyChange(syncthing.android.BR.lastFile);
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
        return invalid != null ? invalid : folder.invalid;
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
        return state;
    }

    @Bindable
    public long getGlobalFiles() {
        return globalFiles;
    }

    @Bindable
    public long getGlobalBytes() {
        return globalBytes;
    }

    @Bindable
    public long getLocalFiles() {
        return localFiles;
    }

    @Bindable
    public long getLocalBytes() {
        return localBytes;
    }

    @Bindable
    public long getNeedFiles() {
        return needFiles;
    }

    @Bindable
    public long getNeedBytes() {
        return needBytes;
    }

    @Bindable
    public int getCompletion() {
        if (state == ModelState.SYNCING) {
            return globalBytes != 0
                    ? Math.min(100, Math.round(100f * inSyncBytes / globalBytes))
                    : 100;
        } else if (state == ModelState.SCANNING) {
            return scanProgress != null
                    ? Math.min(100, Math.round(100f * scanProgress.current / scanProgress.total))
                    : 100;
        } else {
            return 0;
        }
    }

    @Bindable
    public boolean getIgnorePatterns() {
        return ignorePatterns;
    }

    @Bindable
    public String getSharedWith() {
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
            return "";
        } else {
            StringBuilder b = new StringBuilder(sharedNames.get(0));
            for (int ii=1; ii<sharedNames.size(); ii++) {
                b.append(", ").append(sharedNames.get(ii));
            }
            return b.toString();
        }
    }

    @Bindable
    public int getNumFolderErrors() {
        return (errors != null) ? errors.size() : 0;
    }

    @Bindable
    public LastFile getLastFile() {
        return stats != null ? stats.lastFile : null;
    }

    @BindingAdapter("folderLastFile")
    public static void folderLastFile(TextView view, LastFile lastFile) {
        if (lastFile != null) {
            if (StringUtils.isEmpty(lastFile.filename)) {
                view.setText(R.string.na);
            } else {
                int r = lastFile.deleted ? R.string.deleted : R.string.updated;
                String n = view.getContext().getString(r) + " " + lastFile.filename;
                view.setText(n);
            }
        }
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
