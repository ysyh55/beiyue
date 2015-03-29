/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.data;

import static me.zheteng.cbreader.data.CnBetaContract.FavoriteEntry;

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

    static final int FAVORITE = 100;
    static final int FAVORITE_WITH_SID = 101;

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CnBetaContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, CnBetaContract.PATH_FAVORITE, FAVORITE);
        matcher.addURI(authority, CnBetaContract.PATH_FAVORITE + "/#", FAVORITE_WITH_SID);

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
            case FAVORITE: {
                retCursor = mOpenHelper.getReadableDatabase().query(FavoriteEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case FAVORITE_WITH_SID: {
                String sid = FavoriteEntry.getSidFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(FavoriteEntry.TABLE_NAME, projection,
                        FavoriteEntry.COLUMN_SID + " = ? ", new String[] {sid}, null, null, sortOrder);
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
            case FAVORITE:
                return FavoriteEntry.CONTENT_TYPE;
            case FAVORITE_WITH_SID:
                return FavoriteEntry.CONTENT_ITEM_TYPE;

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
            case FAVORITE: {
                long _id = db.insert(FavoriteEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = FavoriteEntry
                            .buildFavoriteUri(values.getAsString(FavoriteEntry.COLUMN_SID));
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
            case FAVORITE:
                //                db.beginTransaction();
                //                try {
                //                    for (ContentValues value : values) {
                //                        long _id = db.insert(FavoriteEntry.TABLE_NAME, null, value);
                //                        if (_id != -1) {
                //                            returnCount++;
                //                        }
                //                    }
                //                    db.setTransactionSuccessful();
                //                } finally {
                //                    db.endTransaction();
                //                }
                //                getContext().getContentResolver().notifyChange(uri, null);
                //
                //                break;
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
            case FAVORITE:
                rowsDeleted = db.delete(
                        FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVORITE_WITH_SID:
                String sid = FavoriteEntry.getSidFromUri(uri);
                rowsDeleted = db.delete(
                        FavoriteEntry.TABLE_NAME, FavoriteEntry.COLUMN_SID + " = ?", new String[] {sid});
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
            case FAVORITE:
                rowsUpdated = db.update(FavoriteEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case FAVORITE_WITH_SID:
                String sid = FavoriteEntry.getSidFromUri(uri);
                rowsUpdated = db.update(
                        FavoriteEntry.TABLE_NAME, values,
                        FavoriteEntry.COLUMN_SID + " = ? ",
                        new String[] {sid});
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
