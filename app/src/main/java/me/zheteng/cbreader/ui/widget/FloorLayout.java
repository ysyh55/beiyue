/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui.widget;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.CmntDetail;
import me.zheteng.cbreader.model.Cmntdict;

/**
 * 盖楼View
 */
public class FloorLayout extends LinearLayout {
    private Context mContext;
    private int mDensity;
    private Drawable mBoundDrawable;

    private Map<String, CmntDetail> mCmntStore;
    private List<Cmntdict> mCmntParents;
    private int mFloor = 0;

    private ViewFactory mFactory;

    public FloorLayout(Context context) {
        this(context, null);

    }

    public FloorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;
        mFactory = new ViewFactory();
        this.setOrientation(LinearLayout.VERTICAL);
        mDensity = (int) context.getResources().getDisplayMetrics().density;
    }

    public void setData(List<Cmntdict> parents, Map<String, CmntDetail> store) {
        mCmntParents = parents;
        mCmntStore = store;
        init();
    }

    private void init() {
        mFloor = 0;
        removeAllViews();
        if (mCmntParents != null) {
            for (Cmntdict mCmntParent : mCmntParents) {
                View view = mFactory.buildSubFloor(mCmntParent, this);
                addView(view);
            }
        }

        layoutChildren();
        mBoundDrawable = mContext.getResources().getDrawable(R.drawable.sub_floor_bound);
    }

    private void layoutChildren() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);

            LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layout.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

            int margin = Math.min((count - i - 1), 4) * mDensity;
            layout.leftMargin = margin;
            layout.rightMargin = margin;
            if (i == count - 1) {
                layout.topMargin = 0;
            } else {
                layout.topMargin = Math.min((count - i), 4) * mDensity;
            }
            view.setLayoutParams(layout);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int count = getChildCount();

        if (null != mBoundDrawable && count > 0) {
            for (int j = count - 1; j >= 0; j--) {
                View view = getChildAt(j);
                mBoundDrawable.setBounds(view.getLeft(), view.getLeft(), view.getRight(), view.getBottom());
                mBoundDrawable.draw(canvas);
            }
        }
        super.dispatchDraw(canvas);
    }

    private class ViewFactory {
        public View buildSubFloor(Cmntdict dict, ViewGroup group) {
            CmntDetail detail = mCmntStore.get(dict.tid);

            LayoutInflater inflater =
                    (LayoutInflater) group.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.comment_sub_floor, group, false);

            RelativeLayout show = (RelativeLayout) view.findViewById(R.id.show_sub_floor_content);
            RelativeLayout hide = (RelativeLayout) view.findViewById(R.id.hide_sub_floor_content);
            show.setVisibility(View.VISIBLE);
            hide.setVisibility(View.GONE);

            TextView floorNum = (TextView) view.findViewById(R.id.sub_floor_num);
            TextView username = (TextView) view.findViewById(R.id.sub_floor_username);
            TextView content = (TextView) view.findViewById(R.id.sub_floor_content);

            floorNum.setText(String.valueOf(++mFloor));
            username.setText(detail.name);
            content.setText(detail.comment);
            return view;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.getChildCount() <= 0) {
            setMeasuredDimension(0, 0);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
