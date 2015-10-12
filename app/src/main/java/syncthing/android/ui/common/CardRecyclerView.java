/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *   Modified 2015 OpenSilk Productions LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package syncthing.android.ui.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;


public class CardRecyclerView extends RecyclerView implements CanExpand.OnExpandListener {

    boolean wobbleOnExpand = true;

    public CardRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addItemDecoration(new DividerItemDecoration(context, attrs));
    }

    public void setWobbleOnExpand(boolean wobbleOnExpand) {
        this.wobbleOnExpand = wobbleOnExpand;
    }

    //--------------------------------------------------------------------------
    // Expand and Collapse animator
    //--------------------------------------------------------------------------

    @Override
    public void onExpandStart(CanExpand viewCard, View expandingLayout) {
        ExpandCollapseHelper.animateExpanding(expandingLayout,viewCard,this,wobbleOnExpand);
    }

    @Override
    public void onCollapseStart(CanExpand viewCard, View expandingLayout) {
        ExpandCollapseHelper.animateCollapsing(expandingLayout,viewCard,this,wobbleOnExpand);
    }

}
