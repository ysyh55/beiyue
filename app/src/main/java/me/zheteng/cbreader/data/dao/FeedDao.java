/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.data.dao;

import static me.zheteng.cbreader.data.CnBetaContract.FeedMessageEntry;

import android.content.ContentValues;
import android.content.Context;
import me.zheteng.cbreader.model.FeedMessage;

/**
 * TODO 记得添加注释
 */
public class FeedDao {

    public static int setReaded(Context context, FeedMessage message) {
        if (message.isReaded()) {
            return 0;
        } else {
            message.setReaded(true);
            ContentValues cv = new ContentValues();
            cv.put(FeedMessageEntry.COLUMN_READED, 1);
            return context.getContentResolver().update(FeedMessageEntry.buildFeedUri(message.getGuid()), cv, null,
                    null);
        }
    }
}
