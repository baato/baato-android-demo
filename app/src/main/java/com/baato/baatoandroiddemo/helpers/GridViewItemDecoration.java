package com.baato.baatoandroiddemo.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class GridViewItemDecoration extends RecyclerView.ItemDecoration {

    // Used to draw item split line.
    protected Drawable itemDividerDrawable;
    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public GridViewItemDecoration(Context context) {
        //itemDividerDrawable = context.getDrawable(R.drawable.divider);
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        itemDividerDrawable = a.getDrawable(0);
        a.recycle();
    }

    public GridViewItemDecoration(Context context, int resId) {
        itemDividerDrawable = ContextCompat.getDrawable(context, resId);
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        drawHorizontal(canvas, parent);
        drawVertical(canvas, parent);
    }

    // Get grid column count.
    private int getColumnCount(RecyclerView parent)
    {
        int columnCount = -1;

        // Get RecyclerView layout manager.
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        // If RecyclerView use GridLayoutManager.
        if (layoutManager instanceof GridLayoutManager)
        {
            columnCount = ((GridLayoutManager) layoutManager).getSpanCount();
        }
        // If RecyclerView use StaggeredGridLayoutManager.
        else if (layoutManager instanceof StaggeredGridLayoutManager)
        {
            columnCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return columnCount;
    }

    public void drawHorizontal(Canvas canvas, RecyclerView parent)
    {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + layoutParams.bottomMargin;
            int bottom = top + this.itemDividerDrawable.getIntrinsicHeight();

            int left = child.getLeft() - layoutParams.leftMargin;
            int right = child.getRight() + layoutParams.rightMargin + this.itemDividerDrawable.getIntrinsicWidth();

            this.itemDividerDrawable.setBounds(left, top, right, bottom);
            this.itemDividerDrawable.draw(canvas);
        }
    }

    public void drawVertical(Canvas canvas, RecyclerView parent)
    {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getTop() - layoutParams.topMargin;
            int bottom = child.getBottom() + layoutParams.bottomMargin;

            int left = child.getRight() + layoutParams.rightMargin;
            int right = left + this.itemDividerDrawable.getIntrinsicWidth();

            this.itemDividerDrawable.setBounds(left, top, right, bottom);
            this.itemDividerDrawable.draw(canvas);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        int columnCount = getColumnCount(parent);
        int childCount = parent.getAdapter().getItemCount();

        // Do not need to draw bottom horizontal divider for the last row.
        if (isLastRaw(parent, itemPosition, columnCount, childCount))
        {
            outRect.set(0, 0, this.itemDividerDrawable.getIntrinsicWidth(), 0);
        }
        // Do not need to draw right vertical divider for the last column.
        else if (isLastColumn(parent, itemPosition, columnCount, childCount))
        {
            outRect.set(0, 0, 0, this.itemDividerDrawable.getIntrinsicHeight());
        } else
        {
            outRect.set(0, 0, this.itemDividerDrawable.getIntrinsicWidth(), this.itemDividerDrawable.getIntrinsicHeight());
        }
    }

    // Check whether this is the last row.
    private boolean isLastRaw(RecyclerView parent, int position, int columnCount, int childCount)
    {
        boolean ret = false;

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        // If RecyclerView use GridLayoutManager.
        if (layoutManager instanceof GridLayoutManager)
        {
            // Get the integral multiple times of columnCount value
            childCount = childCount - childCount % columnCount;
            // The last row item position bigger than or equal to integral multiple times of columnCount value.
            if (position >= childCount) {
                ret = true;
            }
        }
        // If RecyclerView use StaggeredGridLayoutManager.
        else if (layoutManager instanceof StaggeredGridLayoutManager)
        {
            // Get StaggeredGridLayoutManager scroll orientation.
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();

            // If vertical scroll.
            if (orientation == StaggeredGridLayoutManager.VERTICAL)
            {
                // Get the integral multiple times of columnCount value
                childCount = childCount - childCount % columnCount;
                // The last row item position bigger than or equal to integral multiple times of columnCount value.
                if (position >= childCount) {
                    ret = true;
                }
            } else
            // If horizontal scroll.
            {
                // This condition means the last row.
                if ((position + 1) % columnCount == 0)
                {
                    ret = true;
                }
            }
        }
        return ret;
    }

    // Check whether this is the last column.
    private boolean isLastColumn(RecyclerView parent, int position, int columnCount, int childCount)
    {
        boolean ret = false;

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        // If RecyclerView use GridLayoutManager.
        if (layoutManager instanceof GridLayoutManager)
        {
            // This condition means the last column.
            if((position + 1) % columnCount == 0)
            {
                ret = true;
            }
        }
        // If RecyclerView use StaggeredGridLayoutManager.
        else if (layoutManager instanceof StaggeredGridLayoutManager)
        {
            // Get scroll orientation
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            // If scroll vertical.
            if (orientation == StaggeredGridLayoutManager.VERTICAL)
            {
                // This is the last column
                if ((position + 1) % columnCount == 0)
                {
                    ret = true;
                }
            }
            // If scroll horizontal.
            else
            {
                // The last column item position bigger than or equal to integral multiple times of columnCount value.
                childCount = childCount - childCount % columnCount;
                if (position >= childCount)
                    ret = true;
            }
        }
        return ret;
    }
}
