/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.model;

import android.content.Context;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.TimeUtils;

/**
 * TODO 记得添加注释
 */
public class CmntDetail {
    public String tid;
    public String pid;
    public String sid;
    public String date;
    public String name;
    public String host_name;
    public String comment;
    public String score;
    public String reason;
    public String userid;
    public String icon;
    public String readableTime;

    public String getReadableTime(Context context) {
        if (readableTime == null) {
            if (PrefUtils.isShowTimeElapse(context)) {
                readableTime = TimeUtils.getEllapseTime(TimeUtils.fmt.parseDateTime(date).getMillis());
            } else {
                readableTime = date;
            }
        }
        return readableTime;
    }
}
