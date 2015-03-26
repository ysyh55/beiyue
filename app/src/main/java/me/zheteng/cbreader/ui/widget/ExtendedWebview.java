/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ExtendedWebview extends WebView {
    public ExtendedWebview(Context context) {
        super(context);
    }

    public ExtendedWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
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