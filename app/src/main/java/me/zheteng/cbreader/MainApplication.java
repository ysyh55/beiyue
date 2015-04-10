/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import me.zheteng.cbreader.data.CnBetaDBHelper;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.volley.BitmapLruCache;
import me.zheteng.cbreader.utils.volley.PersistentCookieStore;

/**
 * 全局Application类
 */
public class MainApplication extends Application {

    public static RequestQueue requestQueue;
    public static ImageLoader imageLoader;
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

        // CookieStore is just an interface, you can implement it and do things like
        // save the cookies to disk or what ever.
        CookieStore cookieStore = new PersistentCookieStore(this);
        CookieManager manager = new CookieManager( cookieStore, CookiePolicy.ACCEPT_ALL );
        CookieHandler.setDefault(manager);

        requestQueue = Volley.newRequestQueue(this);
        imageLoader = new ImageLoader(requestQueue, new BitmapLruCache());

        if (!PrefUtils.isTopicsTableDone(this)) {
            new FillTopicTableTask().execute();
        }
    }

    private class FillTopicTableTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            CnBetaDBHelper mOpenHelper = new CnBetaDBHelper(MainApplication.this);
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            long start = System.currentTimeMillis();
            // 保证这些操作都完成
            db.beginTransaction();

            String[] sqls = getResources().getStringArray(R.array.topic_sqls);
            for (int i = 0; i < sqls.length; i++) {
                db.execSQL(sqls[i]);
            }
            // 操作结束
            db.setTransactionSuccessful();
            db.endTransaction();

            db.close();

            Log.d("MainApplication", "初始化消耗时间:" + (System.currentTimeMillis() - start) + "ms");
            PrefUtils.markTopicsTableDone(MainApplication.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }
}
