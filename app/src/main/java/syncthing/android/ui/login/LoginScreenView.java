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
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.core.util.VersionUtils;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import syncthing.android.R;

/**
 * Created by drew on 3/10/15.
 */
public class LoginScreenView extends CoordinatorLayout {

    @Inject ToolbarOwner mToolbarOwner;
    @Inject LoginPresenter mPresenter;

    CompositeSubscription subscriptions;
    syncthing.android.ui.login.LoginScreenViewBinding binding;

    public LoginScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTransitionGroup();
        if (!isInEditMode()) {
            LoginComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        binding = DataBindingUtil.bind(this);
        binding.setPresenter(mPresenter);
        if (!isInEditMode()) {
            mPresenter.takeView(this);
        }
        binding.executePendingBindings();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        subscribeChanges();
        binding.editServerUrl.requestFocus();
        if (!isInEditMode()) {
            mToolbarOwner.attachToolbar(binding.toolbar);
            mToolbarOwner.setConfig(ActionBarConfig.builder().setTitle(R.string.login).build());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mToolbarOwner.detachToolbar(binding.toolbar);
        mPresenter.dropView(this);
        if (subscriptions != null) subscriptions.unsubscribe();
        dismissKeyboard();
    }

    void subscribeChanges() {
        subscriptions = new CompositeSubscription(
                RxTextView.textChanges(binding.editAlias)
                        .subscribe(mPresenter.viewModel::setAlias),
                RxTextView.textChanges(binding.editServerUrl)
                        .subscribe(mPresenter.viewModel::setHost),
                RxTextView.textChanges(binding.editServerPort)
                        .subscribe(mPresenter.viewModel::setPort),
                RxTextView.textChanges(binding.editUser)
                        .subscribe(mPresenter.viewModel::setUser),
                RxTextView.textChanges(binding.editPassword)
                        .subscribe(mPresenter.viewModel::setPass),
                RxCompoundButton.checkedChanges(binding.useTls)
                        .subscribe(mPresenter.viewModel::setTls),
                RxTextView.editorActions(binding.editPassword)
                        .subscribe(id -> {
                            mPresenter.submit(null);
                        })
        );
    }

    void dismissKeyboard() {
        final InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @TargetApi(21)
    private void applyTransitionGroup() {
        if (VersionUtils.hasLollipop()) {
            setTransitionGroup(true);
        }
    }

}
