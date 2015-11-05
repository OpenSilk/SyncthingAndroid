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

package syncthing.android.ui.sessionsettings;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import syncthing.android.R;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.Ignores;
import syncthing.api.model.SystemInfo;

/**
 * Created by drew on 3/23/15.
 */
public class EditIgnoresScreenView extends CoordinatorLayout {

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.desc_filename) TextView descFilename;
    @InjectView(R.id.edit_ignores) EditText editIgnores;

    @Inject ToolbarOwner mToolbarOwner;
    @Inject EditIgnoresPresenter mPresenter;

    public EditIgnoresScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            EditIgnoresComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mToolbarOwner.attachToolbar(toolbar);
            mToolbarOwner.setConfig(mPresenter.getToolbarConfig());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
    }

    @OnClick(R.id.btn_help)
    void showHelp() {
        mPresenter.openHelp();
    }

    @OnClick(R.id.btn_cancel)
    void doCancel() {
        mPresenter.dismissDialog();
    }

    @OnClick(R.id.btn_save)
    void save() {
        if (!mPresenter.validateIgnores(editIgnores.getText().toString())) {
            return;
        }
        mPresenter.saveIgnores(editIgnores.getText().toString());
    }

    void initialize(FolderConfig folder, SystemInfo system, Ignores ignores) {
        descFilename.setText(folder.path + system.pathSeparator + ".stignore");
        if (ignores.ignore != null && ignores.ignore.length > 0) {
            editIgnores.setText(StringUtils.join(ignores.ignore, "\n"));
        }
    }
}
