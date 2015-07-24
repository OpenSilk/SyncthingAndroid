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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import syncthing.android.R;
import timber.log.Timber;

public class WelcomeScreenPagerAdapter extends PagerAdapter {

    Context context;
    WelcomePresenter presenter;

    int page;
    View view;

    public WelcomeScreenPagerAdapter(Context context, WelcomePresenter presenter) {
        super();
        this.context = context;
        this.presenter = presenter;
    }

    @Override
    public int getCount() {
        if (presenter.skipTutorial)
            return 1;
        return 6;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int page) {
        final int layout;
        switch (page) {
            default:
            case 0:
                layout = R.layout.screen_welcome_p0;
                break;
            case 1:
                layout = R.layout.screen_welcome_p1;
                break;
            case 2:
                layout = R.layout.screen_welcome_p2;
                break;
            case 3:
                layout = R.layout.screen_welcome_p3;
                break;
            case 4:
                layout = R.layout.screen_welcome_p4;
                break;
            case 5:
                layout = R.layout.screen_welcome_pl;
                break;
        }
        this.page = page;
        this.view = View.inflate(context, layout, null);
        ((ViewPager) container).addView(view);
        reload();
        return view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return null;
            case 1:
                return "Syncthing";
            case 2:
                return "Devices";
            case 3:
                return "Folders";
            case 4:
                return "Remote Control";
            case 5:
                return "Start";
        }
        return null;
    }

    public void reload() {
        if (view == null)
            return;
        Button btnClose = (Button) view.findViewById(R.id.btn_close);
        TextView text = (TextView) view.findViewById(R.id.welcomePLText);
        ImageView logo = (ImageView) view.findViewById(R.id.welcomePLBanner);
        if (btnClose == null)
            return;
        if (presenter.generating) {
            btnClose.setOnClickListener((View view) -> {
                Timber.d("Cancel setup");
                presenter.cancelGeneration();
            });
            logo.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.rotate_indefinitely));
            return;
        }
        logo.clearAnimation();
        logo.setImageResource(R.drawable.logo_icon_300);
        if (presenter.isSuccess()) {
            text.setText(R.string.welcome_pl_ready_title);
            btnClose.setText(R.string.welcome_pl_ready);
            btnClose.setOnClickListener((View view) -> {
                Timber.d("Close setup");
                presenter.exitSuccess();
            });
            logo.setOnClickListener((View view) -> {
                Timber.d("Close logo");
                presenter.exitSuccess();
            });
        } else {
            text.setText(R.string.welcome_pl_generating_failed);
            btnClose.setText(R.string.welcome_pl_remote);
            btnClose.setOnClickListener((View view) -> {
                Timber.d("Close setup");
                presenter.exitCanceled();
            });
            logo.setOnClickListener((View view) -> {
                Timber.d("Close logo");
                presenter.exitCanceled();
            });
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int page, Object object) {
        ((ViewPager) container).removeView((View) object);
    }
}
