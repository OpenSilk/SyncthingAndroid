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
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensilk.common.core.mortar.DaggerService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Subscriber;
import syncthing.android.R;
import timber.log.Timber;

/**
 * Created by drew on 10/14/15.
 */
public class WelcomePage5View extends RelativeLayout {

    @Inject WelcomePresenter mPresenter;

    @InjectView(R.id.btn1) Button btn1;
    @InjectView(R.id.btn2) Button btn2;
    @InjectView(R.id.btn_bar) ViewGroup btnBar;
    @InjectView(R.id.welcomePLText) TextView text;
    @InjectView(R.id.welcomePLBanner) ImageView logo;

    public WelcomePage5View(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            WelcomePageComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        switch (mPresenter.getState()) {
            case NONE:
                goToState1();
                break;
            case GENERATING:
                goToState2();
                break;
            case SUCCESS:
                goToState3();
                break;
            case ERROR:
                gotoState4();
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.setInitializedSubscriber(null);
    }

    //failed
    void gotoState4() {
        logo.clearAnimation();
        logo.setImageResource(R.drawable.logo_icon_300);
        text.setText(R.string.welcome_pl_generating_failed);
        btnBar.setVisibility(VISIBLE);
        btn1.setVisibility(GONE);
        btn2.setText(R.string.welcome_pl_remote);
    }

    //success
    void goToState3() {
        logo.clearAnimation();
        logo.setImageResource(R.drawable.logo_icon_300);
        text.setText(R.string.welcome_pl_ready_title);
        btnBar.setVisibility(VISIBLE);
        btn1.setVisibility(GONE);
        btn2.setText(R.string.welcome_pl_ready);
    }

    void goToState2() {
        logo.setImageResource(R.drawable.logo_loading_icon_300);
        logo.startAnimation(
                AnimationUtils.loadAnimation(getContext(), R.anim.rotate_indefinitely));
        text.setText(R.string.welcome_pl_generating);
        //TODO cancelling doesnt actually cancel
        btnBar.setVisibility(GONE);
//        btn1.setVisibility(GONE);
//        btn2.setText(R.string.welcome_pl_cancel);
    }

    void goToState1() {
        logo.clearAnimation();
        logo.setImageResource(R.drawable.logo_icon_300);
        text.setText(R.string.welcome_pl_initialize_local_instance);
        btnBar.setVisibility(VISIBLE);
        btn1.setText(R.string.no);
        btn2.setText(R.string.yes);
    }

    @OnClick(R.id.btn1)
    void handleBtn1() {
        switch (mPresenter.getState()) {
            case NONE:
                //don't want to initialized
                mPresenter.exitCanceled();
                break;
            default:
                Timber.e("Btn1 pressed in invalid state %s", mPresenter.getState());
                break;
        }
    }

    @OnClick(R.id.btn2)
    void handleBtn2() {
        switch (mPresenter.getState()) {
            case NONE:
                //want to initialized localinstance
                mPresenter.initializeInstance();
                mPresenter.setInitializedSubscriber(mSuccessSubscriber);
                goToState2();
                break;
            case GENERATING:
                //want to cancel generation
                mPresenter.cancelGeneration();
                break;
            case SUCCESS:
                //gen success
                mPresenter.exitSuccess();
                break;
            case ERROR:
                //gen failed
                mPresenter.exitCanceled();
                break;
        }
    }

    private final Subscriber<Boolean> mSuccessSubscriber = new Subscriber<Boolean>() {
        @Override public void onCompleted() { }

        @Override
        public void onError(Throwable e) {
            onNext(false);
        }

        @Override
        public void onNext(Boolean aBoolean) {
            Timber.d("onNext(%s)", aBoolean);
            if (aBoolean) {
                goToState3();
            } else {
                gotoState4();
            }
        }
    };

}
