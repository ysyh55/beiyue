/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.github.ksoichiro.android.observablescrollview.Scrollable;

import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class MyShowHideToolbarListener extends ShowHideToolbarListener {
    private int mPrevScrollY;
    private View mTabsContainer;

    public MyShowHideToolbarListener(BaseActivity mainActivity,
                                     Scrollable view, View tabsContainer) {
        super(mainActivity, view);
        this.mTabsContainer = tabsContainer;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
        ViewConfiguration vc = ViewConfiguration.get(this.mActivity);
        boolean scrollUp = mPrevScrollY < scrollY; // 页面向上滚动
        int slop = Math.abs(scrollY - mStartY);

        if (slop > vc.getScaledTouchSlop()) {
            if (scrollUp) {
                if (mActivity.isToolbarShow()) {
                    scrollTabUp();
                    mActivity.hideToolbar();
                }
            } else {
                if (!mActivity.isToolbarShow()) {
                    mActivity.showToolbar();
                    scrollTabDown();
                }
            }
        }
        mPrevScrollY = scrollY;
    }

    protected void scrollTabDown() {
        if (mTabsContainer == null) {
            return;
        }

        ViewPropertyAnimator animator1 = mTabsContainer.animate().translationY(0).setInterpolator(new
                DecelerateInterpolator());
        animator1.start();
    }

    protected void scrollTabUp() {
        if (mTabsContainer == null) {
            return;
        }
        mTabsContainer.animate().translationY(-mActivity.getToolbar().getHeight()).setInterpolator(new
                AccelerateInterpolator())
                .start();
    }

}