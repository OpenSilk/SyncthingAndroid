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

package syncthing.android.ui.folderpicker;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ToolbarOwner;
import org.opensilk.common.ui.recycler.RecyclerListCoordinator;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import syncthing.android.R;

/**
 * Created by drew on 11/3/15.
 */
public class FolderPickerView extends RecyclerListCoordinator {

    @Inject ToolbarOwner mToolbarOwner;
    @Inject FolderPickerPresenter mPresenter;
    @Inject FolderPickerViewAdapter mAdapter;

    @InjectView(R.id.toolbar) Toolbar toolbar;

    public FolderPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            FolderPickerComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        mList.setHasFixedSize(true);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setAdapter(mAdapter);
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mPresenter.takeView(this);
            mToolbarOwner.attachToolbar(toolbar);
            mToolbarOwner.setConfig(ActionBarConfig.builder()
                    .setTitle(mPresenter.path)
                    .setMenuConfig(mPresenter.getToolbarConfig()).build());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        mToolbarOwner.detachToolbar(toolbar);
    }

    void addAll(List<String> strings) {
        mAdapter.addAll(strings);
    }

    void onComplete() {
        if (mAdapter.isEmpty()) {
            showEmpty(true);
        }
    }
}
