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
import android.widget.TextView;

import org.opensilk.common.core.mortar.DaggerService;

import syncthing.android.ui.common.BindsCard;
import syncthing.android.ui.common.Card;
import syncthing.android.ui.common.NoDecorate;

/**
 * Created by drew on 3/1/15.
 */
public class HeaderCardView extends TextView implements BindsCard, NoDecorate {

    //@InjectView(R.id.title) TextView title;
    //@InjectView(R.id.btn_add) Button btnAdd;

    final SessionPresenter presenter;

    HeaderCard item;

    public HeaderCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        presenter = DaggerService.<SessionComponent>getDaggerComponent(getContext()).presenter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //ButterKnife.inject(this);
    }

    //@OnClick(R.id.btn_add)
    void doAdd() {
        if (item != null && item.addAction != null) {
            item.addAction.call(presenter);
        }
    }

    @Override
    public Card getCard() {
        return item;
    }

    @Override
    public void bind(Card card) {
        item = ((HeaderCard) card);
        setText(item.title);
        //title.setText(item.title);
        //btnAdd.setText(item.buttonText);
    }

    @Override
    public void reset() {
        item = null;
    }
}
