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

package syncthing.android.ui.welcome;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import org.opensilk.common.core.mortar.DaggerService;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class WelcomeScreenPagerView extends ViewPager {

    Context context;
    @Inject WelcomePresenter mPresenter;
    WelcomeScreenPagerAdapter adapter;
    boolean splash;

    public WelcomeScreenPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (!isInEditMode()) {
            WelcomeComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
            this.adapter = new WelcomeScreenPagerAdapter(context, mPresenter);
            this.splash = true;
            setAdapter(adapter);
            addOnPageChangeListener(new SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int page) {
                    splash = false;
                    mPresenter.updatePage(page);
                }
            });
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isInEditMode())
            return;
        ButterKnife.inject(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void reload() {
        adapter.reload();
    }

    public void setPage(int page, boolean reload) {
        if (getCurrentItem() == page) {
            adapter.reload();
        } else {
            setCurrentItem(page);
        }
    }

    public void hideSplash() {
        if (getCurrentItem() == 0) {
            nextPage();
        }
    }

    public void nextPage() {
        if (getCurrentItem() < adapter.getCount()) {
            setCurrentItem(getCurrentItem() + 1);
        }
    }

}
