/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.model;

/**
 * TODO 记得添加注释
 */
public class Comment {

    private String name;
    private String content;
    private long pubDate;
    private int support;
    private int oppose;

    private String feedGuid;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPubDate() {
        return pubDate;
    }

    public void setPubDate(long pubDate) {
        this.pubDate = pubDate;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public int getOppose() {
        return oppose;
    }

    public void setOppose(int oppose) {
        this.oppose = oppose;
    }

    public String getFeedGuid() {
        return feedGuid;
    }

    public void setFeedGuid(String feedGuid) {
        this.feedGuid = feedGuid;
    }
}
