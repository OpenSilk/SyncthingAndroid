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

import android.content.Intent;
import android.os.Bundle;

import org.opensilk.common.mortar.ActivityResultsActivity;
import org.opensilk.common.mortar.ActivityResultsOwner;
import org.opensilk.common.mortar.MortarActivity;
import org.opensilk.common.mortar.ScreenScoper;

import javax.inject.Inject;

import mortar.MortarScope;

/**
 * Created by drew on 3/10/15.
 */
public abstract class MortarFragmentActivity extends MortarActivity implements
        FragmentManagerOwner.Activity,
        ActivityResultsActivity {

    @Inject protected FragmentManagerOwner mFragmentManagerOwner;
    @Inject protected ActivityResultsOwner mActivityResultsOwner;

    protected abstract void performInjection();

    @Override
    protected void onPreCreateScope(MortarScope.Builder buidler) {
        buidler.withService(ScreenScoper.SERVICE_NAME, new ScreenScoper())
                .withService(LayoutCreator.SERVICE_NAME, new LayoutCreator());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        performInjection();
        mFragmentManagerOwner.takeView(this);
        mActivityResultsOwner.takeView(this);
        setResult(RESULT_CANCELED);
    }

    @Override
    protected void onDestroy() {
        mFragmentManagerOwner.dropView(this);
        mActivityResultsOwner.dropView(this);
        super.onDestroy();
    }

    @Override
    public void setResultAndFinish(int resultCode, Intent data) {
        setResult(resultCode, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mActivityResultsOwner.onActivityResult(requestCode, resultCode, data);
    }
}
