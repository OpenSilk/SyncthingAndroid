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

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.android.ui.common.CardRecyclerView;

/**
 * Created by drew on 3/15/15.
 */
public class ManageScreenView extends RelativeLayout {

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.recyclerview) CardRecyclerView list;

    final ManageScreenAdapter adapter = new ManageScreenAdapter();
    @Inject ToolbarOwner mToolbarOwner;
    @Inject ManagePresenter mPresenter;

    public ManageScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ManageComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mToolbarOwner.attachToolbar(toolbar);
        mToolbarOwner.setConfig(ActionBarConfig.builder().setTitle(R.string.manage_devices).build());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mToolbarOwner.detachToolbar(toolbar);
        mPresenter.dropView(this);
    }

    @OnClick(R.id.btn_done)
    public void onDone() {
        mPresenter.exitActivity();
    }

    @OnClick(R.id.btn_add)
    public void onAddDevice() {
        mPresenter.openAddScreen();
    }

    void load(List<Credentials> creds, Credentials def) {
        adapter.setDevices(creds, def);
    }
}
