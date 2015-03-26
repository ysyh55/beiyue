/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui.widget;

import com.github.ksoichiro.android.observablescrollview.ObservableWebView;

import android.content.Context;
import android.util.AttributeSet;

/**
 * TODO 记得添加注释
 */
public class ExtendedObservableWebView extends ObservableWebView {
    public ExtendedObservableWebView(Context context) {
        super(context);
    }

    public ExtendedObservableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedObservableWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean canScroll(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) {
            return false;
        }
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }
}
