/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.model;

import java.util.List;

import org.jsoup.Jsoup;

/**
 * Represents one RSS message
 */
public class FeedMessage {

    private String title;
    private String content;
    private String description;
    private String link;
    private String guid;
    private long pubDate;
    private boolean readed = false;
    private String plainText;

    private int commentCount;
    private List<Comment> comments;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }


    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setPubDate(long pubDate) {
        this.pubDate = pubDate;
    }

    public long getPubDate() {
        return pubDate;
    }
    @Override
    public String toString() {
        return "FeedMessage [title=" + title + ", description=" + description
                + ", link=" + link + ", guid=" + guid
                + "]";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isReaded() {
        return readed;
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }

    /**
     * 如果readed为1,代表已读
     * @param readed 1或0
     */
    public void setReaded(int readed) {
        this.readed = (readed == 1);
    }

    public String getPlainText() {
        if (plainText == null) {
            plainText = Jsoup.parse(description).text();
            return plainText;
        } else {
            return plainText;
        }
    }
}