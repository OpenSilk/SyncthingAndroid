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

package org.opensilk.common.ui.mortarfragment;

import android.os.Bundle;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.PauseAndResumeActivity;
import org.opensilk.common.ui.mortar.PauseAndResumePresenter;

/**
 * Created by drew on 9/28/15.
 */
public abstract class PauseAndResumeFragment extends MortarFragment implements PauseAndResumeActivity {

    private boolean mIsResumed;
    private PauseAndResumePresenter mPausesAndResumesPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PauseAndResumeFragmentComponent cmp = DaggerService.getDaggerComponent(getScope());
        mPausesAndResumesPresenter = cmp.pauseAndResumePresenter();
        mPausesAndResumesPresenter.takeView(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsResumed = true;
        mPausesAndResumesPresenter.activityResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsResumed = false;
        mPausesAndResumesPresenter.activityPaused();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPausesAndResumesPresenter.dropView(this);
    }

    @Override
    public boolean isRunning() {
        return mIsResumed;
    }

}
