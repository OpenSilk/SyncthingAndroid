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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.opensilk.common.mortar.ScreenScoper;
import org.opensilk.common.mortarfragment.MortarDialogFragment;
import org.opensilk.common.mortarfragment.MortarFragment;
import org.opensilk.common.mortarfragment.MortarFragmentUtils;

import butterknife.ButterKnife;
import mortar.MortarScope;
import syncthing.android.R;
import timber.log.Timber;

/**
 * Created by drew on 3/16/15.
 */
public class EditFragment extends MortarDialogFragment implements EditFragmentPresenter.DialogOwner {

    public static EditFragment newFolderInstance() {
        EditFragment f = new EditFragment();
        Bundle b = new Bundle();
        b.putBoolean("isFolder", true);
        b.putInt("title", R.string.add_folder);
        f.setArguments(b);
        return f;
    }

    public static EditFragment newFolderInstance(String folderId) {
        EditFragment f = new EditFragment();
        Bundle b = new Bundle();
        b.putBoolean("isFolder", true);
        b.putInt("title", R.string.edit_folder);
        b.putString("folder", folderId);
        b.putBoolean("editmode", true);
        f.setArguments(b);
        return f;
    }

    public static EditFragment newFolderInstance(String folderId, String deviceId) {
        EditFragment f = new EditFragment();
        Bundle b = new Bundle();
        b.putBoolean("isFolder", true);
        b.putInt("title", R.string.share_folder);
        b.putString("folder", folderId);
        b.putString("device", deviceId);
        b.putBoolean("editmode", true);
        f.setArguments(b);
        return f;
    }

    public static EditFragment newFolderIgnoresInstance(String folderId) {
        EditFragment f = new EditFragment();
        Bundle b = new Bundle();
        b.putBoolean("isFolder", true);
        b.putBoolean("isIgnores", true);
        b.putInt("title", R.string.ignore_patterns);
        b.putString("folder", folderId);
        f.setArguments(b);
        return f;
    }

    public static EditFragment newDeviceInstance() {
        EditFragment f = new EditFragment();
        Bundle b = new Bundle();
        b.putBoolean("isFolder", false);
        b.putBoolean("isDevice", true);
        b.putInt("title", R.string.add_device);
        f.setArguments(b);
        return f;
    }

    public static EditFragment newDeviceInstance(String deviceId) {
        EditFragment f = new EditFragment();
        Bundle b = new Bundle();
        b.putBoolean("isFolder", false);
        b.putBoolean("isDevice", true);
        b.putInt("title", R.string.edit_device);
        b.putString("device", deviceId);
        b.putBoolean("editmode", true);
        f.setArguments(b);
        return f;
    }

    public static EditFragment newSettingsInstance() {
        EditFragment f = new EditFragment();
        Bundle b = new Bundle();
        b.putBoolean("isFolder", false);
        b.putBoolean("isDevice", false);
        b.putInt("title", R.string.settings);
        b.putBoolean("editmode", true);
        f.setArguments(b);
        return f;
    }


    @Override
    protected Object getScreen() {
        boolean isFolder = getArguments().getBoolean("isFolder");
        boolean isDevice = getArguments().getBoolean("isDevice");
        if (isFolder) {
            boolean isIgnores = getArguments().getBoolean("isIgnores");
            String fid = getArguments().getString("folder");
            String did = getArguments().getString("device");
            if (isIgnores) {
                return new EditIgnoresScreen(fid); //ignores
            } else if (fid != null && did != null) {
                return new EditFolderScreen(fid, did);//share
            } else if (fid != null) {
                return new EditFolderScreen(fid);//Edit
            } else {
                return new EditFolderScreen();//Add
            }
        } else if (isDevice) {
            String did = getArguments().getString("device");
            if (did != null) {
                return new EditDeviceScreen(did);//edit
            } else {
                return new EditDeviceScreen();//add
            }
        } else {
            return new SettingsScreen();
        }
    }

    @Override
    protected String getScopeName() {
        //hack for editignores
        return super.getScopeName() + Integer.toHexString(getArguments().getInt("title"));
    }

    @Override
    protected MortarScope findOrMakeScope() {
        //This scope descends from SessionScope
        MortarScope parentScope = ((MortarFragment) getParentFragment()).getScope();
        MortarScope scope = parentScope.findChild(getScopeName());
        if (scope != null) {
            Timber.d("Reusing fragment scope %s", getScopeName());
        }
        if (scope == null) {
            ScreenScoper scoper = getScreenScoperService();
            scope = scoper.getScreenScope(getResources(),  parentScope, getScopeName(), getScreen());
            Timber.d("Created new fragment scope %s", getScopeName());
        }
        return scope;
    }

    //bridge for presenters to dismiss the dialog without context
    EditFragmentPresenter mFragmetnPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean editing = getArguments().getBoolean("editmode");
        setStyle(STYLE_NORMAL, editing ? R.style.SessionEditDialogTheme_Edit : R.style.SessionEditDialogTheme);
        mFragmetnPresenter = MortarFragmentUtils.<EditFragmentComponent>getDaggerComponent(mScope).fragmentPresenter();
        mFragmetnPresenter.takeView(this);
    }

    @Override
    public void onDestroy() {
        mFragmetnPresenter.dropView(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.screen_edit_container, container, false);
        View v = super.onCreateView(inflater, container, savedInstanceState);
        vg.addView(v);
        return vg;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar tb = ButterKnife.findById(view, R.id.edit_toolbar);
        tb.setTitle(getArguments().getInt("title"));
        tb.setNavigationOnClickListener(v -> dismiss());
    }

}
