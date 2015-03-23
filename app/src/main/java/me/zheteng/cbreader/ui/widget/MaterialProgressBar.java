/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class MaterialProgressBar extends View {

    private CircularProgressDrawable mDrawable;

    public MaterialProgressBar(Context context) {
        this(context, null);
    }

    public MaterialProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDrawable = new CircularProgressDrawable(Color.parseColor("#1E88E5"), 3);
        mDrawable.setCallback(this);
        if (getVisibility() == VISIBLE) {
            mDrawable.start();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mDrawable.draw(canvas);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mDrawable != null) {
            if (visibility == VISIBLE) {
                mDrawable.start();
            } else {
                mDrawable.stop();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawable.setBounds(0, 0, w, h);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mDrawable || super.verifyDrawable(who);
    }
}