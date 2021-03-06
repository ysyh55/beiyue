/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.model;

import me.zheteng.cbreader.utils.TimeUtils;

/**
 * NewsPoJO
 */
public class NewsComment {
    public boolean supportAvailable = true; //表示是否可点
    public boolean againstAvailable = true; //表示是否可点
    public String tid;
    public String username;
    public String content;
    public String created_time;
    public String support;
    public String against;

    private String readableTime;

    public String getReadableTime() {
        if (readableTime == null) {
            readableTime = TimeUtils.getEllapseTime(TimeUtils.fmt.parseDateTime(created_time).getMillis());
        } else {
            readableTime = created_time;
        }
        return readableTime;
    }

    public String getUsername() {
        if (username.equals("")) {
            return "匿名用户";
        }
        return username;
    }
}
