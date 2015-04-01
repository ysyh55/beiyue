/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui.widget;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 快速返回的RecyclerView //TODO 未完成
 */
public class QuickReturnRecyclerView extends ObservableRecyclerView{
    public QuickReturnRecyclerView(Context context) {
        super(context);
    }

    public QuickReturnRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuickReturnRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

}
