/*
 * Copyright (C) 2015 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensilk.common.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import syncthing.android.R;

/**
 * Created by drew on 11/24/14.
 */
public class RecyclerListFrame extends FrameLayout {

    @InjectView(R.id.list_container) protected View mListContainer;
    @InjectView(R.id.recyclerview) protected RecyclerView mList;
    @InjectView(R.id.empty_view) protected View mEmptyView;
    @InjectView(R.id.empty_text) protected TextView mEmptyText;
    @InjectView(R.id.loading_progress) protected ProgressBar mLoadingProgress;

    protected boolean mLoadingShown;
    protected boolean mListShown;
    protected boolean mEmptyShown;

    public RecyclerListFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    public RecyclerView getListView() {
        return mList;
    }

    public void setEmptyText(int stringRes) {
        mEmptyText.setText(getContext().getString(stringRes));
    }

    public void setLoading(boolean shown) {
        if (mLoadingShown == shown) {
            return;
        }
        mLoadingShown = shown;
        // this is on a delay so we always animate
        mLoadingProgress.setAnimation(AnimationUtils.loadAnimation(
                getContext(), shown ? android.R.anim.fade_in : android.R.anim.fade_out));
        if (shown) {
            mLoadingProgress.setVisibility(VISIBLE);
        } else {
            mLoadingProgress.setVisibility(GONE);
        }
    }

    public void setListShown(boolean shown, boolean animate) {
        setLoading(!shown);
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (animate) {
            mListContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), shown ? android.R.anim.fade_in : android.R.anim.fade_out));
        } else {
            mListContainer.clearAnimation();
        }
        if (shown) {
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            mListContainer.setVisibility(View.GONE);
        }
    }

    public void setListEmpty(boolean shown, boolean animate) {
        setLoading(false);
        if (mEmptyShown == shown) {
            return;
        }
        mEmptyShown = shown;
        if (shown) {
            if (mListShown) {
                if (animate) {
                    mList.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
                    mEmptyView.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                } else {
                    mList.clearAnimation();
                    mEmptyView.clearAnimation();
                }
                mList.setVisibility(GONE);
                mEmptyView.setVisibility(VISIBLE);
            } else {
                mList.setVisibility(GONE);
                mEmptyView.setVisibility(VISIBLE);
                setListShown(true, animate);
            }
        } else {
            if (mListShown) {
                if (animate) {
                    mEmptyView.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
                    mList.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                } else {
                    mEmptyView.clearAnimation();
                    mList.clearAnimation();
                }
                mEmptyView.setVisibility(GONE);
                mList.setVisibility(VISIBLE);
            } else {
                mEmptyView.setVisibility(GONE);
                mList.setVisibility(VISIBLE);
                setListShown(true, animate);
            }
        }
    }
}