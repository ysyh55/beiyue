/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.data;

import static me.zheteng.cbreader.data.CnBetaContract.CommentEntry;
import static me.zheteng.cbreader.data.CnBetaContract.FeedMessageEntry;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * TODO 记得添加注释
 */
public class CnBetaProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private CnBetaDBHelper mOpenHelper;

    static final int FEED = 100;
    static final int FEED_MESSAGE_WITH_ID = 101;

    static final int COMMENT = 200;
    static final int COMMENT_FOR_FEED = 201;

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CnBetaContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, CnBetaContract.PATH_FEED_MESSAGE, FEED);
        matcher.addURI(authority, CnBetaContract.PATH_FEED_MESSAGE + "/*", FEED_MESSAGE_WITH_ID);

        matcher.addURI(authority, CnBetaContract.PATH_COMMENT, COMMENT);
        matcher.addURI(authority, CnBetaContract.PATH_COMMENT + "/*", COMMENT_FOR_FEED);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new CnBetaDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case FEED_MESSAGE_WITH_ID: {
                String guid = FeedMessageEntry.getGuidFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(FeedMessageEntry.TABLE_NAME, projection,
                        FeedMessageEntry.COLUMN_GUID + " = ? ", new String[] {guid}, null, null, sortOrder);
                break;
            }
            // "weather/*"
            case FEED: {
                retCursor = mOpenHelper.getReadableDatabase().query(FeedMessageEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            // "weather"
            case COMMENT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CommentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case COMMENT_FOR_FEED: {
                String guid = CommentEntry.getFeedIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CommentEntry.TABLE_NAME,
                        projection,
                        CommentEntry.COLUMN_FEED_ID + " = ? ",
                        new String[] {guid},
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case FEED:
                return FeedMessageEntry.CONTENT_TYPE;
            case FEED_MESSAGE_WITH_ID:
                return FeedMessageEntry.CONTENT_ITEM_TYPE;
            case COMMENT:
                return CommentEntry.CONTENT_TYPE;
            case COMMENT_FOR_FEED:
                return CommentEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FEED: {
                long _id = db.insert(FeedMessageEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = FeedMessageEntry.buildFeedUri(values.getAsString(CommentEntry.COLUMN_FEED_ID));
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case COMMENT: {
                long _id = db.insert(CommentEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CommentEntry.buildCommentUri(values.getAsString(CommentEntry.COLUMN_FEED_ID));
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case FEED:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(FeedMessageEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);

                break;
            case COMMENT:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(CommentEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            
            default:
                return super.bulkInsert(uri, values);
        }
        return returnCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) {
            selection = "1";
        }
        switch (match) {
            case FEED:
                rowsDeleted = db.delete(
                        FeedMessageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COMMENT:
                rowsDeleted = db.delete(
                        CommentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case FEED:
                rowsUpdated = db.update(FeedMessageEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case FEED_MESSAGE_WITH_ID:
                String guid = FeedMessageEntry.getGuidFromUri(uri);
                rowsUpdated = db.update(FeedMessageEntry.TABLE_NAME, values, FeedMessageEntry.COLUMN_GUID + " = ? ",
                        new String[]{guid});
                break;
            case COMMENT:
                rowsUpdated = db.update(CommentEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
