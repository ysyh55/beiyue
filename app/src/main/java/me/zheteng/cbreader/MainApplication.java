/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.Application;

/**
 * 全局Application类
 */
public class MainApplication extends Application {

    public static RequestQueue requestQueue;
    public static final int[] DRAWER_HEADER_BACKGROUND = {
            R.drawable.drawer_header_1,
            R.drawable.drawer_header_2,
            R.drawable.drawer_header_3,
            R.drawable.drawer_header_4,
            R.drawable.drawer_header_5,
            R.drawable.drawer_header_6,
    };

    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = Volley.newRequestQueue(this);
    }

}
