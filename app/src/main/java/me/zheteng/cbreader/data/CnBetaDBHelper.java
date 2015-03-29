/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.data;

import static me.zheteng.cbreader.data.CnBetaContract.FavoriteEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库OpenHelper
 * TODO 升级模式
 */
public class CnBetaDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "cnbeta.db";

    public CnBetaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createAllTables(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 保证这些操作都完成
        db.beginTransaction();

        // 要求onCreate里的create table操作都带有 if not exists
        createAllTables(db);

        // 保存现有的表的字段
        List<String> columnsFavorite = getColumns(db, FavoriteEntry.TABLE_NAME);

        // 备份现有的表
        backupTableFavorite(db);

        // 重新创建所有表
        createAllTables(db);

        // 把备份的表中所有无用字段去除
        columnsFavorite.retainAll(getColumns(db, FavoriteEntry.TABLE_NAME));

        // 恢复所有的表
        restoreTableFavorite(db, columnsFavorite);

        // 移除所有的备份表
        removeBackupTableFavorite(db);

        // 操作结束
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void removeBackupTableFavorite(SQLiteDatabase db) {
        db.execSQL("DROP TABLE '_temp_" + FavoriteEntry.TABLE_NAME + "'");
    }

    private void restoreTableFavorite(SQLiteDatabase db, List<String> columnsFavorite) {
        String cols = join(columnsFavorite, ",");
        db.execSQL(String.format(
                "INSERT INTO %s (%s) SELECT %s from _temp_%s",
                FavoriteEntry.TABLE_NAME, cols, cols, FavoriteEntry.TABLE_NAME));
    }

    private void backupTableFavorite(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE "
                + FavoriteEntry.TABLE_NAME
                + " RENAME TO _temp_"
                + FavoriteEntry.TABLE_NAME);
    }

    private void createAllTables(SQLiteDatabase db) {
        createFavoriteTable(db);
    }

    private void createFavoriteTable(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE IF NOT EXISTS " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                FavoriteEntry.COLUMN_SID + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_ARTICLE + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_COMMENT + " TEXT , " +
                FavoriteEntry.COLUMN_NEWSCONTENT + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_CREATE_TIME + " TEXT NOT NULL, " +

                " UNIQUE ( " + FavoriteEntry.COLUMN_SID +
                " ) ON CONFLICT REPLACE " +
                " );";
        db.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    public static List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> ar = null;
        Cursor c = null;
        try {
            c = db.rawQuery("select * from " + tableName + " limit 1", null);
            if (c != null) {
                ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return ar;
    }

    public static String join(List<String> list, String delim) {
        StringBuilder buf = new StringBuilder();
        int num = list.size();
        for (int i = 0; i < num; i++) {
            if (i != 0) {
                buf.append(delim);
            }
            buf.append((String) list.get(i));
        }
        return buf.toString();
    }
}
