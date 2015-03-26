/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.umeng.analytics.MobclickAgent;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * 基础Activity
 */
public class BaseActivity extends ActionBarActivity {

    protected Toolbar mToolbar;
    private boolean mIsToolbarShow = true;

    protected  void showToolbar() {
        if (mToolbar == null) {
            return;
        }
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        mIsToolbarShow = true;
    }

    protected void hideToolbar() {
        if (mToolbar == null) {
            return;
        }
        mToolbar.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        mIsToolbarShow = false;
    }

    public boolean isToolbarShow() {
        return mIsToolbarShow;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
