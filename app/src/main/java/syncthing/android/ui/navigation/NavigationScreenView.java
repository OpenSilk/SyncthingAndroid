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

package syncthing.android.ui.navigation;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.opensilk.common.core.mortar.DaggerService;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import syncthing.android.R;
import syncthing.android.model.Credentials;
import syncthing.android.ui.common.CardRecyclerView;

/**
 * Created by drew on 3/10/15.
 */
public class NavigationScreenView extends LinearLayout {

    @InjectView(android.R.id.list) CardRecyclerView list;

    final NavigationPresenter presenter;

    final NavigationRecyclerAdapter adapter;

    public NavigationScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<NavigationComponent>getDaggerComponent(getContext()).presenter();
        adapter = new NavigationRecyclerAdapter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
//        adapter.setExpandListener(list);
//        list.setWobbleOnExpand(false);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
    }

    @OnClick(R.id.app_settings)
    void openAppSettings() {
        presenter.startSettingsActivity();
    }

    @OnClick(R.id.manage_devices)
    void openDevicemanagement() {
        presenter.startDeviceManageActivity();
    }

    void load(List<Credentials> creds) {
        adapter.setDevices(creds);
    }

}
