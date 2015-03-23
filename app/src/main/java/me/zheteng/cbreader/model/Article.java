/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.model;

import me.zheteng.cbreader.utils.TimeUtils;

/**
 * 文章列表的文章
 */
public class Article {
    public int sid;
    public String title;
    public String pubtime;
    public String summary;
    public int topic;
    public int counter;
    public int comments;
    public float ratings;
    public float score;
    public String ratings_story;
    public String score_story;
    public String topic_logo;
    public String thumb;

    private String readableTime;

    public String getReadableTime() {
        if (readableTime == null) {
            readableTime = TimeUtils.getElapsedTime(TimeUtils.fmt.parseDateTime(pubtime).getMillis());
        }
        return readableTime;
    }
}
