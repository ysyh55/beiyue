/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.model;

import org.jsoup.Jsoup;

import android.os.Parcel;
import android.os.Parcelable;
import me.zheteng.cbreader.utils.TimeUtils;

/**
 * 文章列表的文章
 */
public class Article implements Parcelable {
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

    public void readFromNewsContent(NewsContent content) {
        title = content.title;
        pubtime = content.time;
        summary = Jsoup.parse(content.hometext).text();
        topic = Integer.parseInt(content.topic);
        counter = Integer.parseInt(content.counter);
        comments = content.comments;
        ratings = Float.parseFloat(content.ratings);
        score = Float.parseFloat(content.score);
        ratings_story = content.ratings_story;
        score_story = content.score_story;
        topic_logo = content.thumb;
        thumb = content.thumb;

    }

    public String getReadableTime() {
        if (readableTime == null) {
            readableTime = TimeUtils.getElapsedTime(TimeUtils.fmt.parseDateTime(pubtime).getMillis());
        }
        return readableTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.sid);
        dest.writeString(this.title);
        dest.writeString(this.pubtime);
        dest.writeString(this.summary);
        dest.writeInt(this.topic);
        dest.writeInt(this.counter);
        dest.writeInt(this.comments);
        dest.writeFloat(this.ratings);
        dest.writeFloat(this.score);
        dest.writeString(this.ratings_story);
        dest.writeString(this.score_story);
        dest.writeString(this.topic_logo);
        dest.writeString(this.thumb);
        dest.writeString(this.readableTime);
    }

    public Article() {
    }

    private Article(Parcel in) {
        this.sid = in.readInt();
        this.title = in.readString();
        this.pubtime = in.readString();
        this.summary = in.readString();
        this.topic = in.readInt();
        this.counter = in.readInt();
        this.comments = in.readInt();
        this.ratings = in.readFloat();
        this.score = in.readFloat();
        this.ratings_story = in.readString();
        this.score_story = in.readString();
        this.topic_logo = in.readString();
        this.thumb = in.readString();
        this.readableTime = in.readString();
    }

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        public Article createFromParcel(Parcel source) {
            return new Article(source);
        }

        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}
