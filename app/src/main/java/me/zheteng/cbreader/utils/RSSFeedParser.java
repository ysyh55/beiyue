/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
import me.zheteng.cbreader.model.Comment;
import me.zheteng.cbreader.model.Feed;
import me.zheteng.cbreader.model.FeedMessage;

public abstract class RSSFeedParser {
    static final String TITLE = "title";
    static final String LANGUAGE = "language";
    static final String COPYRIGHT = "copyright";
    static final String LINK = "link";
    static final String ITEM = "item";
    static final String PUB_DATE = "pubDate";
    static final String GUID = "guid";
    static final String CONTENT_ENCODED = "content:encoded";
    private static final String LOG_TAG = "RSSFeedParser";

    public RSSFeedParser() {

    }

    public Feed readFeed(Reader in) throws XmlPullParserException, IOException {
        Feed feed = null;
        boolean isFeedHeader = true;
        // Set header values intial to the empty string
        String content = "";
        String title = "";
        String link = "";
        String language = "";
        String copyright = "";
        String pubdate = "";
        String guid = "";

        // First create a new XmlPullParserFactory
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(in);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withLocale(Locale.CHINA);
        // read the XML document
        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xpp.getEventType() == XmlPullParser.START_DOCUMENT) {
                xpp.next();
                Log.d(LOG_TAG, "Start document");
            } else if (xpp.getEventType() == XmlPullParser.START_TAG) {
                String localPart = xpp.getName();
                switch (localPart) {
                    case ITEM:
                        if (isFeedHeader) {
                            isFeedHeader = false;
                            feed = new Feed(title, link, content, language,
                                    copyright, pubdate);
                        }
                        xpp.next();
                        break;
                    case TITLE:
                        title = xpp.nextText();
                        break;
                    case CONTENT_ENCODED:
                        content = xpp.nextText();
                        break;
                    case LINK:
                        link = xpp.nextText();
                        break;
                    case GUID:
                        guid = xpp.nextText();
                        break;
                    case LANGUAGE:
                        language = xpp.nextText();
                        break;
                    case PUB_DATE:
                        pubdate = xpp.nextText();
                        break;
                    case COPYRIGHT:
                        copyright = xpp.nextText();
                        break;
                    default:
                        xpp.next();
                        break;
                }

                if (xpp.getEventType() != XmlPullParser.END_TAG) {
                    xpp.next();
                }

            } else if (xpp.getEventType() == XmlPullParser.END_TAG) {
                if (xpp.getName().equals(ITEM)) {
                    FeedMessage message = new FeedMessage();
                    message.setContent(content);
                    message.setGuid(guid);
                    message.setLink(link);
                    message.setTitle(title);

                    DateTime dateTime = fmt.parseDateTime(pubdate);
                    message.setPubDate(dateTime.getMillis());

                    message.setDescription(getDescriptionFromContent(content));
                    message.setComments(getCommentFromContent(content, guid));

                    feed.getMessages().add(message);

                }
                xpp.next();
            } else if (xpp.getEventType() == XmlPullParser.TEXT) {
                xpp.next();
            }
        }

        Log.d(LOG_TAG, "End document");
        return feed;
    }

    private String getCharacterData(XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        String result = "";
        int event = xpp.getEventType();
        if (event == XmlPullParser.TEXT) {
            result = xpp.getText();
        }
        return result;
    }

    public abstract List<Comment> getCommentFromContent(String cotent, String guid);

    public abstract String getDescriptionFromContent(String content);

}