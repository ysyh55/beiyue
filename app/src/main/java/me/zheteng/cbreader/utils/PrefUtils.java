/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * TODO 记得添加注释
 */
public class PrefUtils {

    public static final String NAME_CACHE = "app_cache";

    public static final String KEY_ARTICLES = "key_articles";

    public static final String KEY_COMMENT_ORDER = "pref_comment_order";

    /**
     * 保存首页缓存
     *
     * @param context context
     * @param json    首页缓存json串
     */
    public static void saveCacheOfKey(Context context, String key, String json) {
        SharedPreferences sp = context.getSharedPreferences(NAME_CACHE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, json);
        editor.apply();
    }

    /**
     * 获取首页的缓存
     *
     * @param context context
     */
    public static String getCacheOfKey(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(NAME_CACHE, Context.MODE_PRIVATE);

        return sp.getString(key, null);
    }

    public static boolean getIsCommentLatestFirst(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getBoolean(KEY_COMMENT_ORDER, true);
    }
}
