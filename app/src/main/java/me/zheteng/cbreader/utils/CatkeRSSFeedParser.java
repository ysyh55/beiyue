/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import me.zheteng.cbreader.model.Comment;

/**
 * http://cnbeta.catke.com/feed 解析
 */
public class CatkeRSSFeedParser extends RSSFeedParser{

    private static final String ITEM = "dl";
    private static final String NAME_AND_DATE = "dt";
    private static final String CONTENT_AND_SUPPORT = "dd";

    private static final String P = "p";



    @Override
    public List<Comment> getCommentFromContent(String cotent, String guid) {
        String[] splited = cotent.split("<h5>热门评论</h5>");

        if (splited.length < 2) {
            return Collections.emptyList();
        } else {
            List<Comment> result = new ArrayList<>();

            String name= "";
            String content= "";
            long pubDate= 0;
            int support= 0;
            int oppose= 0;

            boolean isFirstP = true;

            // First create a new XmlPullParserFactory
            XmlPullParserFactory factory = null;
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            try {
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(splited[1]));

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                    switch (xpp.getEventType()) {
                        case XmlPullParser.TEXT:
                        case XmlPullParser.START_DOCUMENT:
                            xpp.next();
                            break;
                        case XmlPullParser.START_TAG:
                            if(xpp.getName().equalsIgnoreCase(NAME_AND_DATE)) {
                                String[] nameAndDate = xpp.nextText().split("\\|");
                                name = nameAndDate[0].trim();
                                DateTime dateTime = fmt.parseDateTime(nameAndDate[1].trim());
                                pubDate = dateTime.getMillis();
                            } else if (xpp.getName().equalsIgnoreCase(P)) {
                                if (isFirstP) {
                                    content = xpp.nextText().trim();
                                    isFirstP = false;
                                } else {
                                    String[] suAndOp = xpp.nextText().split("\\|");
                                    support = Integer.parseInt(suAndOp[0].trim().replaceAll("支持：", ""));
                                    oppose = Integer.parseInt(suAndOp[1].trim().replaceAll("反对：", ""));
                                    isFirstP = true;
                                }

                            }
                            xpp.next();
                            break;
                        case XmlPullParser.END_TAG:
                            if (xpp.getName().equalsIgnoreCase(ITEM)) {
                                Comment comment = new Comment();
                                comment.setName(name);
                                comment.setContent(content);
                                comment.setPubDate(pubDate);
                                comment.setSupport(support);
                                comment.setOppose(oppose);
                                comment.setFeedGuid(guid);
                                result.add(comment);
                            }
                            xpp.next();
                            break;
                        default:
                            xpp.next();
                            break;
                    }
                }

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }
    }

    @Override
    public String getDescriptionFromContent(String content) {
        return content.split("<h5>热门评论</h5>")[0].trim();
    }
}
