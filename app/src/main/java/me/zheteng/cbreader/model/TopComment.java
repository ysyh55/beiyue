/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.model;

import android.text.TextUtils;

/**
 * 热门评论的条目
 */
public class TopComment {
    public String id;
    public String comment;
    public String sid;
    public String username;
    public String subject;

    public String getUsername() {
        return TextUtils.isEmpty(username) ? "匿名用户" : username;
    }
}
