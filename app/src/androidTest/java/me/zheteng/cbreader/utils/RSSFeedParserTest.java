/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import android.test.AndroidTestCase;
import me.zheteng.cbreader.model.Feed;
import me.zheteng.cbreader.model.FeedMessage;

public class RSSFeedParserTest extends AndroidTestCase {

    public void testReadFeed() throws Exception {
        RSSFeedParser parser = new RSSFeedParser("http://www.vogella.com/article.rss");
        Feed feed = parser.readFeed();
        System.out.println(feed);
        for (FeedMessage message : feed.getMessages()) {
            System.out.println(message);

        }
    }
}