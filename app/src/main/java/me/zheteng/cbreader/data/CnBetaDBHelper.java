/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.data;

import static me.zheteng.cbreader.data.CnBetaContract.CommentEntry;
import static me.zheteng.cbreader.data.CnBetaContract.FeedMessageEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库OpenHelper
 */
public class CnBetaDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "cnbeta.db";

    public CnBetaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_FEED_TABLE = "CREATE TABLE " + FeedMessageEntry.TABLE_NAME + " (" +
                FeedMessageEntry._ID + " INTEGER PRIMARY KEY," +
                FeedMessageEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FeedMessageEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                FeedMessageEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                FeedMessageEntry.COLUMN_GUID + " TEXT NOT NULL, " +
                FeedMessageEntry.COLUMN_LINK + " TEXT , " +
                FeedMessageEntry.COLUMN_PUB_DATE + " INTEGER , " +
                FeedMessageEntry.COLUMN_COMMENT_COUNT + " INTEGER , " +
                FeedMessageEntry.COLUMN_READED + " INTEGER , " +
                " UNIQUE ( " + FeedMessageEntry.COLUMN_GUID +" ) ON CONFLICT REPLACE " +
                " );";

        final String SQL_CREATE_COMMENT_TABLE = "CREATE TABLE " + CommentEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                CommentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                CommentEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_FEED_ID + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_SUPPORT + " INTEGER NOT NULL," +
                CommentEntry.COLUMN_OPPOSE + " INTEGER, " +
                CommentEntry.COLUMN_PUB_DATE + " INTEGER, " +
                " UNIQUE ( " + CommentEntry.COLUMN_FEED_ID +" , " + CommentEntry.COLUMN_PUB_DATE +
                " ) ON CONFLICT IGNORE, " +
                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + CommentEntry.COLUMN_FEED_ID + ") REFERENCES " +
                FeedMessageEntry.TABLE_NAME + " (" + FeedMessageEntry.COLUMN_GUID + ") " +

                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_FEED_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_COMMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FeedMessageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CommentEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
