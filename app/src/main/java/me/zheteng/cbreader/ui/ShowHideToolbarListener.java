/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.Scrollable;

import android.view.ViewConfiguration;

/**
 * 滚动时隐藏显示toolbar
 *
 */
public class ShowHideToolbarListener implements ObservableScrollViewCallbacks {
    protected BaseActivity mActivity;
    private int mPrevScrollY;
    protected int mStartY;
    private Scrollable mView;

    public ShowHideToolbarListener(BaseActivity mainActivity, Scrollable view) {
        mActivity = mainActivity;
        mView = view;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
        ViewConfiguration vc = ViewConfiguration.get(mActivity);
        boolean scrollUp = mPrevScrollY < scrollY; // 页面向上滚动
        int slop = Math.abs(scrollY - mStartY);

        if (slop > vc.getScaledTouchSlop()) {
            if (scrollUp) {
                if (mActivity.isToolbarShow()) {
                    mActivity.hideToolbar();
                }
            } else {
                if (!mActivity.isToolbarShow()) {
                    mActivity.showToolbar();
                }
            }
        }

        mPrevScrollY = scrollY;
    }

    @Override
    public void onDownMotionEvent() {
        mStartY = mView.getCurrentScrollY();
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }
}
