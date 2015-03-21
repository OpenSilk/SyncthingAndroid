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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;


public class CardRecyclerView extends RecyclerView implements CanExpand.OnExpandListener {

    boolean wobbleOnExpand = true;

    public CardRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //addItemDecoration(new DividerItemDecoration(context, attrs));
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

    /**
     * Helper to animate collapse and expand animation
     */
    static class ExpandCollapseHelper {

        /**
         * This method expandes the view that was clicked.
         *
         * @param expandingLayout  layout to expand
         * @param cardView         cardView
         * @param recyclerView     recyclerView
         */
        public static void animateCollapsing(final View expandingLayout, final CanExpand cardView, final RecyclerView recyclerView, boolean wobble) {
            final int origHeight = expandingLayout.getHeight();

            ValueAnimator animator = createHeightAnimator(expandingLayout, origHeight, 0);
            animator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(final Animator animator) {
                    expandingLayout.setVisibility(View.GONE);

                    //reset height else recycled views never show
                    ViewGroup.LayoutParams layoutParams = expandingLayout.getLayoutParams();
                    layoutParams.height = origHeight;
                    expandingLayout.setLayoutParams(layoutParams);

                    cardView.setExpanded(false);//card.setExpanded(true);

                    notifyAdapter(recyclerView, recyclerView.getLayoutManager().getPosition((View)cardView));

//                Card card = cardView.getCard();
//                if (card.getOnCollapseAnimatorEndListener()!=null)
//                    card.getOnCollapseAnimatorEndListener().onCollapseEnd(card);
                }
            });
            if (wobble) {
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(animator, createWobbleAnimator((View)cardView));
                animatorSet.start();
            } else {
                animator.start();
            }
        }

        /**
         * This method collapse the view that was clicked.
         *
         * @param expandingLayout  layout to collapse
         * @param cardView         cardView
         * @param recyclerView     recyclerView
         */
        public static void animateExpanding(final View expandingLayout, final CanExpand cardView, final RecyclerView recyclerView, boolean wobble) {
            /* Update the layout so the extra content becomes visible.*/
            expandingLayout.setVisibility(View.VISIBLE);

            View parent = (View) expandingLayout.getParent();
            final int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.AT_MOST);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            expandingLayout.measure(widthSpec, heightSpec);


            ValueAnimator animator = createHeightAnimator(expandingLayout, 0, expandingLayout.getMeasuredHeight());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                final int listViewHeight = recyclerView.getHeight();
                final int listViewBottomPadding = recyclerView.getPaddingBottom();

                final View v = findDirectChild(expandingLayout, recyclerView);

                @Override
                public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                    if (recyclerView.getLayoutManager().canScrollVertically()) {
                        final int bottom = v.getBottom();
                        if (bottom > listViewHeight) {
                            final int top = v.getTop();
                            if (top > 0) {
                                //recyclerView.scrollBy(0,Math.min(bottom - listViewHeight + listViewBottomPadding, top));
                                recyclerView.smoothScrollBy(0,Math.min(bottom - listViewHeight + listViewBottomPadding + 4, top));

                            }
                        }
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    cardView.setExpanded(true);//card.setExpanded(true);

                    notifyAdapter(recyclerView,recyclerView.getLayoutManager().getPosition((View)cardView));

//                Card card = cardView.getCard();
//                if (card.getOnExpandAnimatorEndListener()!=null)
//                    card.getOnExpandAnimatorEndListener().onExpandEnd(card);

                }
            });
            if (wobble) {
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(animator, createWobbleAnimator((View)cardView));
                animatorSet.start();
            } else {
                animator.start();
            }
        }

        private static View findDirectChild(final View view, final RecyclerView recyclerView) {
            View result = view;
            View parent = (View) result.getParent();
            while (parent != recyclerView) {
                result = parent;
                parent = (View) result.getParent();
            }
            return result;
        }

        public static ValueAnimator createHeightAnimator(final View view, final int start, final int end) {
            ValueAnimator animator = ValueAnimator.ofInt(start, end);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                    int value = (Integer) valueAnimator.getAnimatedValue();

                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = value;
                    view.setLayoutParams(layoutParams);
                }
            });

            return animator;
        }

        public static Animator createWobbleAnimator(final View view) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "rotationX", 0f, 30f);
            objectAnimator.setDuration(100);
            objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "rotationX", 30f, 0f);
            objectAnimator.setDuration(100);
            objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(objectAnimator, objectAnimator2);
            return set;
        }

        /**
         * This method notifies the adapter after setting expand value inside cards
         *
         * @param recyclerView
         */
        public static void notifyAdapter(RecyclerView recyclerView, int position){
            if (recyclerView instanceof CardRecyclerView) {

                CardRecyclerView cardRecyclerView = (CardRecyclerView) recyclerView;
                if (cardRecyclerView.getAdapter()!=null){
                    cardRecyclerView.getAdapter().notifyItemChanged(position);
                }
            }
        }
    }
}
