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
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.opensilk.common.core.mortar.DaggerService;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import syncthing.android.R;
import timber.log.Timber;

public class WelcomeScreenPagerView extends ViewPager {

    Context context;
    WelcomePresenter presenter;
    WelcomeScreenPagerAdapter adapter;
    boolean splash;

    public WelcomeScreenPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (isInEditMode())
            return;
        this.presenter = DaggerService.<WelcomeComponent>getDaggerComponent(getContext()).welcomePresenter();
        this.adapter = new WelcomeScreenPagerAdapter(context, presenter);
        this.splash = true;
        setAdapter(adapter);
        setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int page) {
                splash = false;
                presenter.updatePage(page);
            }
        });
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
