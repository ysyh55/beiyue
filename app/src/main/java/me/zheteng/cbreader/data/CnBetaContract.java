/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * TODO 记得添加注释
 */
public class CnBetaContract {

    public static final String CONTENT_AUTHORITY = "me.zheteng.cbreader";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FEED_MESSAGE = "feed";
    public static final String PATH_COMMENT = "comment";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner class that defines the table contents of the location table */
    public static final class FeedMessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FEED_MESSAGE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FEED_MESSAGE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FEED_MESSAGE;

        // Table name
        public static final String TABLE_NAME = "feed_message";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_GUID = "guid";
        public static final String COLUMN_PUB_DATE = "pub_date";
        public static final String COLUMN_COMMENT_COUNT = "comment_count";
        public static final String COLUMN_READED = "readed";

        public static String getGuidFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * 从guid生成uri
         *
         * @param guid 这条数据的guid
         *
         * @return 代表该条数据的Uri
         */
        public static Uri buildFeedUri(String guid) {
            return CONTENT_URI.buildUpon().appendPath(guid).build();
        }
    }

    public static final class CommentEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENT;

        // Table name
        public static final String TABLE_NAME = "comment";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_PUB_DATE = "pub_date";
        public static final String COLUMN_SUPPORT = "support";
        public static final String COLUMN_OPPOSE = "oppose";
        public static final String COLUMN_FEED_ID = "feed_id";

        public static String getFeedIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * 从guid生成uri
         *
         * @param guid 这条数据的guid
         *
         * @return 代表该条数据的Uri
         */
        public static Uri buildCommentUri(String guid) {
            return CONTENT_URI.buildUpon().appendPath(guid).build();
        }

        //    public static long getStartDateFromUri(Uri uri) {
        //        String dateString = uri.getQueryParameter(COLUMN_DATE);
        //        if (null != dateString && dateString.length() > 0)
        //            return Long.parseLong(dateString);
        //        else
        //            return 0;
        //    }
    }

}
