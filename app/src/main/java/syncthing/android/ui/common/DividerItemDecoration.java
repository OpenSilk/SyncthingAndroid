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

package syncthing.android.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

//https://gist.github.com/fatfingers/233abbae200b5e87297b
//modified to draw dividers on bottom and let views opt out of decoration
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public DividerItemDecoration(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, new int [] { android.R.attr.listDivider });
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    public DividerItemDecoration(Drawable divider) { mDivider = divider; }

    @Override
    public void getItemOffsets (Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mDivider == null) return;
        if (!shouldDecorate(view)) return;

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) outRect.bottom = mDivider.getIntrinsicHeight();
        else outRect.right = mDivider.getIntrinsicWidth();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent) {
        if (mDivider == null) { super.onDrawOver(c, parent); return; }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            final int childCount = parent.getChildCount();

            for (int i=0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                if (shouldDecorate(child)) {
                    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    final int size = mDivider.getIntrinsicHeight();
                    final int top = child.getBottom() + params.bottomMargin;
                    final int bottom = top + size;
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }
            }
        } else { //horizontal
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();
            final int childCount = parent.getChildCount();

            for (int i=0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                if (shouldDecorate(child)) {
                    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    final int size = mDivider.getIntrinsicWidth();
                    final int left = child.getRight() + params.leftMargin;
                    final int right = left + size;
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }
            }
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            return layoutManager.getOrientation();
        } else throw new IllegalStateException("DividerItemDecoration can only be used with a LinearLayoutManager.");
    }

    boolean shouldDecorate(View child) {
        return !(child instanceof NoDecorate);
    }

}
