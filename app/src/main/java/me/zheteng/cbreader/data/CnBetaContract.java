/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * 数据约定
 */
public class CnBetaContract {

    public static final String CONTENT_AUTHORITY = "me.zheteng.cbreader";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TOPIC = "topic";
    public static final String PATH_FAVORITE = "favorite";
    public static final String PATH_NEWSCONTENT = "newscontent";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }


    /**
     * 关于收藏的数据表
     */
    public static final class FavoriteEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        // Table name
        public static final String TABLE_NAME = "favorite";
        public static final String COLUMN_SID = "sid";
        public static final String COLUMN_NEWSCONTENT = "newscontent";
        public static final String COLUMN_ARTICLE = "article";
        public static final String COLUMN_COMMENT = "comment";
        public static final String COLUMN_CREATE_TIME = "create_time";

        public static String getSidFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * 从sid生成uri
         *
         * @param sid 这条数据的guid
         *
         * @return 代表该条数据的Uri
         */
        public static Uri buildFavoriteUri(String sid) {
            return CONTENT_URI.buildUpon().appendPath(sid).build();
        }
    }

    /**
     * 关于topics的数据表
     */
    public static final class TopicEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOPIC).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOPIC;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOPIC;

        // Table name
        public static final String TABLE_NAME = "topics";
        public static final String COLUMN_TID = "tid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_THUMB = "thumb";
        public static final String COLUMN_LETTER = "letter";
        public static final String COLUMN_CHECKED = "checked";

        public static String getTidFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * 从tid生成uri
         *
         * @param tid 这条数据的tid
         *
         * @return 代表该条数据的Uri
         */
        public static Uri buildFavoriteUri(String tid) {
            return CONTENT_URI.buildUpon().appendPath(tid).build();
        }
    }

}
