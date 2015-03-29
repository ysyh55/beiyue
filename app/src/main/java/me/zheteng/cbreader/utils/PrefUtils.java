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
    public static final String KEY_TOP_COUNTER = "key_top_counter";
    public static final String KEY_TOP_COMMENT = "key_top_comment";
    public static final String KEY_TOP_DIG = "key_top_dig";
    public static final String KEY_TOP_10 = "key_top_10";

    public static final String KEY_COMMENT_ORDER = "pref_comment_order";
    public static final String KEY_TOP_COMMENTS = "key_top_comments"; // 热门评论页数据
    public static final String KEY_FONT_SIZE = "key_font_size";

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

    public static boolean isWelcomeDone(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("is_welcome_done", false);

    }

    public static void markWelcomeDone(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("is_welcome_done", true);
        editor.commit();
    }

    public static int getFontSize(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getInt(KEY_FONT_SIZE, 16);
    }

    public static void setFontSize(Context context, int fontSize) {
        if (fontSize < 10 || fontSize > 25) {
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putInt(KEY_FONT_SIZE, fontSize);
        editor.apply();
    }
}
