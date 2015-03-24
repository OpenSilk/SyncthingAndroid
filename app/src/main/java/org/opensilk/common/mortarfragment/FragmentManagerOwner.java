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

package org.opensilk.common.mortarfragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.internal.VersionUtils;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Gravity;

import org.opensilk.common.dagger2.ActivityScope;
import org.opensilk.common.mortar.HasScope;

import javax.inject.Inject;

import mortar.Presenter;
import mortar.bundler.BundleService;

/**
 * Created by drew on 3/10/15.
 */
@ActivityScope
public class FragmentManagerOwner extends Presenter<FragmentManagerOwner.Activity> {

    public interface Activity extends HasScope {
        FragmentManager getSupportFragmentManager();
        @IdRes int getContainerViewId();
    }

    @Inject
    public FragmentManagerOwner() {
    }

    @Override
    protected BundleService extractBundleService(Activity view) {
        return BundleService.getBundleService(view.getScope());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public int addFragment(Fragment frag, String tag, boolean addToBackstack) {
        if (!hasView()) return -1;
        FragmentTransaction ft = getView().getSupportFragmentManager().beginTransaction();
        if (VersionUtils.isAtLeastL()) {
            frag.setEnterTransition(new Explode());
            frag.setExitTransition(new Explode());
        } else {
            ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        ft.add(frag, tag);
        if (addToBackstack) ft.addToBackStack(tag);
        return ft.commit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public int replaceMainContent(Fragment frag, String tag, boolean addToBackstack) {
        if (!hasView()) return -1;
        FragmentTransaction ft = getView().getSupportFragmentManager().beginTransaction();
        if (VersionUtils.isAtLeastL()) {
            frag.setEnterTransition(new Slide(GravityCompat.END));
            frag.setExitTransition(new Slide(GravityCompat.START));
        } else {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
        ft.replace(getView().getContainerViewId(), frag, tag);
        if (addToBackstack) ft.addToBackStack(tag);
        return ft.commit();
    }

    public boolean goBack() {
        if (hasView() && getView().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            return getView().getSupportFragmentManager().popBackStackImmediate();
        }
        return false;
    }
}
