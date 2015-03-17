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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mortar.dagger2support.DaggerService;
import syncthing.android.R;
import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.CardViewWrapper;
import syncthing.api.SessionController;

/**
 * Created by drew on 3/7/15.
 */
public class NotifCardRestartView extends CardViewWrapper {

    @InjectView(R.id.header) ViewGroup header;
    @InjectView(R.id.expand) ViewGroup expand;
    @InjectView(R.id.title) TextView title;

    final SessionController controller;

    public NotifCardRestartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        controller = DaggerService.<SessionComponent>getDaggerComponent(getContext()).sessionController();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @OnClick(R.id.header)
    void doExpandThing() {
        toggleExpanded();
    }

    @OnClick(R.id.btn_restart)
    void restartSyncthing() {
        controller.restart();
    }

    @Override
    public void bind(Card card) {}

    @Override
    public View getExpandView() {
        return expand;
    }
}
