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

package syncthing.android.ui.login;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.core.util.VersionUtils;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.List;

import javax.inject.Inject;

import syncthing.android.R;

/**
 * Created by drew on 3/15/15.
 */
public class ManageScreenView extends RelativeLayout {

    @Inject ManageScreenAdapter mAdapter;
    @Inject ToolbarOwner mToolbarOwner;
    @Inject ManagePresenter mPresenter;

    syncthing.android.ui.login.ManageScreenViewBinding binding;

    public ManageScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTransitionGroup();
        if (!isInEditMode()) {
            ManageComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        binding = DataBindingUtil.bind(this);
        binding.setPresenter(mPresenter);
        binding.executePendingBindings();
        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerview.setAdapter(mAdapter);
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mPresenter.takeView(this);
            mToolbarOwner.attachToolbar(binding.toolbar);
            mToolbarOwner.setConfig(ActionBarConfig.builder().setTitle(R.string.manage_devices).build());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mToolbarOwner.detachToolbar(binding.toolbar);
        mPresenter.dropView(this);
    }

    void addAll(List<ManageDeviceCard> cards, boolean dirty) {
        if (dirty) {
            mAdapter.replaceAll(cards);
        } else {
            mAdapter.addAll(cards);
        }
    }

    void onComplete() {
        //TODO notify if empty
    }

    @TargetApi(21)
    private void applyTransitionGroup() {
        if (VersionUtils.hasLollipop()) {
            setTransitionGroup(true);
        }
    }
}
