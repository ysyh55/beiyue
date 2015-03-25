/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class APIUtils {

    public static final String TOP_TYPE_COUNTER = "counter"; //阅读量
    public static final String TOP_TYPE_COMMENTS = "comments"; //评论数
    public static final String TOP_TYPE_DIG = "dig"; //推荐

    private static final char[] SIGN_BYTES = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};

    public static String getTimestampInApi() {
        return Long.valueOf(System.currentTimeMillis() / 1000L).toString();
    }

    public static String getSign(String paramString) {
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(paramString.getBytes());
            String str = getSign(localMessageDigest.digest()).toLowerCase();
            return str;
        } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
            localNoSuchAlgorithmException.printStackTrace();
        }
        return "";
    }

    public static String getSign(byte[] paramArrayOfByte) {
        StringBuilder localStringBuilder = new StringBuilder(2 * paramArrayOfByte.length);
        for (int i = 0; i < paramArrayOfByte.length; i++) {
            localStringBuilder.append(SIGN_BYTES[((0xF0 & paramArrayOfByte[i]) >>> 4)]);
            localStringBuilder.append(SIGN_BYTES[(0xF & paramArrayOfByte[i])]);
        }
        return localStringBuilder.toString();
    }

    public static String getRecommendCommentUrl() {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.RecommendComment";
        String str3 = str2 + "&timestamp=" + getTimestampInApi();
        String str4 = str3 + "&v=1.0";
        String str5 = str4 + "&sign=" + getSign(
                new StringBuilder().append(str4).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str5;
    }

    public static String getNewsContentUrl(int paramInt) {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.NewsContent";
        String str3 = str2 + "&sid=" + paramInt;
        String str4 = str3 + "&timestamp=" + getTimestampInApi();
        String str5 = str4 + "&v=1.0";
        String str6 = str5 + "&sign=" + getSign(
                new StringBuilder().append(str5).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str6;
    }

    public static String getDoCmtAgainstUrl(int paramInt1, int paramInt2) {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.DoCmt";
        String str3 = str2 + "&op=against";
        String str4 = str3 + "&sid=" + paramInt1;
        String str5 = str4 + "&tid=" + paramInt2;
        String str6 = str5 + "&timestamp=" + getTimestampInApi();
        String str7 = str6 + "&v=1.0";
        String str8 = str7 + "&sign=" + getSign(
                new StringBuilder().append(str7).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str8;
    }

    public static String getDoCmtPublishUrl(int paramInt, String paramString) {
        String str1 = "app_key=10000" + "&content=" + paramString;
        String str2 = str1 + "&format=json";
        String str3 = str2 + "&method=Article.DoCmt";
        String str4 = str3 + "&op=publish";
        String str5 = str4 + "&sid=" + paramInt;
        String str6 = str5 + "&timestamp=" + getTimestampInApi();
        String str7 = str6 + "&v=1.0";
        String str8 = str7 + "&sign=" + getSign(
                new StringBuilder().append(str7).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str8;
    }

    public static String getTodayRankUrl(String type) {
        String str = "app_key=10000" + "&format=json";
        str = str + "&method=Article.TodayRank";
        str = str + "&timestamp=" + getTimestampInApi();
        str = str + "&type=" + type;
        str = str + "&v=1.0";
        str = str + "&sign=" + getSign(
                new StringBuilder().append(str).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str;
        //  "&type=comments";
        //  "&type=counter";
        //  "&type=dig";

    }

    public static String getArticleListsUrl() {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.Lists";
        String str3 = str2 + "&timestamp=" + getTimestampInApi();
        String str4 = str3 + "&v=1.0";
        String str5 = str4 + "&sign=" + getSign(
                new StringBuilder().append(str4).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str5;
    }

    public static String getTopicListUrl(int paramInt) {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.Lists";
        String str3 = str2 + "&timestamp=" + getTimestampInApi();
        String str4 = str3 + "&topicid=" + paramInt;
        String str5 = str4 + "&v=1.0";
        String str6 = str5 + "&sign=" + getSign(
                new StringBuilder().append(str5).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str6;
    }

    public static String getTopicListWithStartUrl(int topicId, int start) {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.Lists";
        String str3 = str2 + "&start_sid=" + start;
        String str4 = str3 + "&timestamp=" + getTimestampInApi();
        String str5 = str4 + "&topicid=" + topicId;
        String str6 = str5 + "&v=1.0";
        String str7 = str6 + "&sign=" + getSign(
                new StringBuilder().append(str6).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str7;
    }

    public static String getTop10Url() {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.Top10";
        String str3 = str2 + "&timestamp=" + getTimestampInApi();
        String str4 = str3 + "&v=1.0";
        String str5 = str4 + "&sign=" + getSign(
                new StringBuilder().append(str4).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str5;
    }

    public static String getCommentListWithPageUrl(int sid, int page) {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.Comment";
        String str3 = str2 + "&page=" + page;
        String str4 = str3 + "&sid=" + sid;
        String str5 = str4 + "&timestamp=" + getTimestampInApi();
        String str6 = str5 + "&v=1.0";
        String str7 = str6 + "&sign=" + getSign(
                new StringBuilder().append(str6).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str7;
    }

    public static String getNavListUrl() {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.NavList";
        String str3 = str2 + "&timestamp=" + getTimestampInApi();
        String str4 = str3 + "&v=1.0";
        String str5 = str4 + "&sign=" + getSign(
                new StringBuilder().append(str4).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str5;
    }

    public static String getArticleListUrl(int topicId, int endSid) {

        //http://api.cnbeta.com/capi?app_key=10000&format=json&method=Article.Lists&timestamp=1427034899587&v=1.0&sign=13e8f708020d0e8f3f8576fa4bb37175
        String str1 = "app_key=10000" + "&end_sid=" + endSid;
        String str2 = str1 + "&format=json";
        String str3 = str2 + "&method=Article.Lists";
        String str4 = str3 + "&timestamp=" + getTimestampInApi();
        String str5 = str4 + "&topicid=" + topicId;
        String str6 = str5 + "&v=1.0";
        String str7 = str6 + "&sign=" + getSign(
                new StringBuilder().append(str6).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str7;
    }

    public static String getDoCmtSupportUrl(int paramInt1, int paramInt2) {
        String str1 = "app_key=10000" + "&format=json";
        String str2 = str1 + "&method=Article.DoCmt";
        String str3 = str2 + "&op=support";
        String str4 = str3 + "&sid=" + paramInt1;
        String str5 = str4 + "&tid=" + paramInt2;
        String str6 = str5 + "&timestamp=" + getTimestampInApi();
        String str7 = str6 + "&v=1.0";
        String str8 = str7 + "&sign=" + getSign(
                new StringBuilder().append(str7).append("&").append("mpuffgvbvbttn3Rc").toString());
        return "http://api.cnbeta.com/capi?" + str8;
    }

}