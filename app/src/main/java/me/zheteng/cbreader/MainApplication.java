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


    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = Volley.newRequestQueue(this);
    }

}
