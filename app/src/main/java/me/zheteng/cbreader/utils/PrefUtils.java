/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import java.util.List;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Topic;

/**
 * 设置读写工具类
 */
public class PrefUtils {

    public static final String NAME_CACHE = "app_cache";

    public static final String KEY_ARTICLES = "key_articles";
    public static final String KEY_TOP_COUNTER = "key_top_counter";
    public static final String KEY_TOP_COMMENT = "key_top_comment";
    public static final String KEY_TOP_DIG = "key_top_dig";
    public static final String KEY_TOP_10 = "key_top_10";
    public static final String KEY_TOPIC = "key_topic_";

    public static final String KEY_COMMENT_ORDER = "pref_comment_order";
    public static final String KEY_TOP_COMMENTS = "key_top_comments"; // 热门评论页数据
    public static final String KEY_FONT_SIZE = "key_font_size";
    public static final String KEY_TOPICS_TABLE_DONE = "key_topics_table_done";
    private static final String KEY_VOLLEY_COOKIE = "key_volley_cookie";
    public static final String DEFAULT_STRING = "";
    public static final String KEY_TOPIC_SUBSCRIPTION = "key_topic_subscription";
    private static final String KEY_IS_NIGHT = "key_is_night";

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

        return sp.getInt(KEY_FONT_SIZE, 18);
    }

    public static void setFontSize(Context context, int fontSize) {
        if (fontSize < 10 || fontSize > 25) {
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putInt(KEY_FONT_SIZE, fontSize);
        editor.apply();
    }

    /**
     * topic数据表是否已经插入好数据
     *
     * @param context
     *
     * @return true or false
     */
    public static boolean isTopicsTableDone(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getBoolean(KEY_TOPICS_TABLE_DONE, false);
    }

    /**
     * 标记topic数据表插入完成
     *
     * @param context
     */
    public static void markTopicsTableDone(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(KEY_TOPICS_TABLE_DONE, true);
        editor.commit();
    }

    public static void saveJsonSessionCookie(Context mContext, String json) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString(KEY_VOLLEY_COOKIE, json);
        editor.apply();
    }

    public static String getJsonSessionCookie(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getString(KEY_VOLLEY_COOKIE, DEFAULT_STRING);
    }

    public static List<Topic> getTopicSubscriptions(Context context) {
        SharedPreferences sp = context.getSharedPreferences(NAME_CACHE, Context.MODE_PRIVATE);

        String json = sp.getString(KEY_TOPIC_SUBSCRIPTION, context.getString(R.string.topics_show_default));
        Gson gson = new Gson();
        return Utils.getListFromArray(gson.fromJson(json, Topic[].class));
    }

    public static void saveTopicSubscriptions(Context context, List<Topic> topics) {
        SharedPreferences.Editor editor = context.getSharedPreferences(NAME_CACHE, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();

        String json = gson.toJson(topics.toArray(), Topic[].class);
        editor.putString(KEY_TOPIC_SUBSCRIPTION, json);
        editor.apply();
    }

    public static boolean isNightMode(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getBoolean(KEY_IS_NIGHT, false);
    }

    public static void toggleNightMode(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(KEY_IS_NIGHT, !isNightMode(context));

        editor.commit();
    }

    public static boolean isShowTimeElapse(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getBoolean(context.getString(R.string.pref_use_elapse_time_key), true);
    }
}
