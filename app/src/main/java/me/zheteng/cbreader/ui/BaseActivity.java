/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.AnimationUtils;
import me.zheteng.cbreader.R;

/**
 * TODO 记得添加注释
 */
public class BaseActivity extends ActionBarActivity {

    protected Toolbar mToolbar;

    protected  void showToolbar() {
        if (mToolbar == null) {
            return;
        }

        mToolbar.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.translate_up_off));
    }

    protected void hideToolbar() {
        if (mToolbar == null) {
            return;
        }

        mToolbar.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.translate_up_on));
    }
}
