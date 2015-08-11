/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.umeng.analytics.MobclickAgent;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import me.zheteng.cbreader.BuildConfig;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.UIUtils;

/**
 * 基础Activity
 */
public class BaseActivity extends ActionBarActivity {

    protected Toolbar mToolbar;
    private boolean mIsToolbarShow = true;

    boolean isNight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isNight = PrefUtils.isNightMode(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = isNight ? getResources().getColor(R.color.night_theme_primary) :
                    getResources().getColor(R.color.theme_primary);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(color);

        }

        int theme = isNight ? R.style.AppThemeDark : R.style.AppTheme;
        int windowBg = isNight ? getResources().getColor(R.color.night_window_bg) :
                getResources().getColor(R.color.background_material_light);

        getWindow().setBackgroundDrawable(new ColorDrawable(windowBg));
        setTheme(theme);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View statusBar = findViewById(R.id.status_placeholder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (statusBar != null) {
                statusBar.getLayoutParams().height = UIUtils.getStatusBarHeight(this);
                if (this instanceof MainActivity) {
                    statusBar.setVisibility(View.GONE);
                } else if (isNight) {
                    statusBar.setBackgroundColor(getResources().getColor(R.color.night_theme_primary_dark));
                }
            }
        } else {
            if (statusBar != null) {
                statusBar.setVisibility(View.GONE);
            }
        }
    }

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
        if (!BuildConfig.DEBUG) {
            MobclickAgent.onResume(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!BuildConfig.DEBUG) {
            MobclickAgent.onPause(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MainApplication.requestQueue != null) {
            MainApplication.requestQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
