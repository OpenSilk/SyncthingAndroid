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

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.opensilk.common.core.mortar.DaggerService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import syncthing.android.R;

public class WelcomeScreenView extends RelativeLayout {

    @Inject WelcomePresenter presenter;
    Context context;
    AlertDialog errorDialog;

    @InjectView(R.id.pager_view) WelcomeScreenPagerView pageView;

    public WelcomeScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (!isInEditMode()) {
            DaggerService.<WelcomeComponent>getDaggerComponent(getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            ButterKnife.inject(this);
            presenter.takeView(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
    }

    void showError(String error) {
        dismissError();
        errorDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.welcome_pl_generating_failed)
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    void dismissError(){
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
        }
    }

    public void setPage(int page) {
        pageView.setPage(page);
    }

    public void hideSplash() {
        pageView.hideSplash();
    }

}
