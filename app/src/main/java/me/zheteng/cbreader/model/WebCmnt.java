/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.model;

import java.util.List;
import java.util.Map;

/**
 * 完整结构见项目根目录的comment.json,这是网页用的json结构,可能失效
 */
public class WebCmnt {
    public Map<String, List<Cmntdict>> cmntdict;
    public List<CmntItem> hotlist;
    public List<CmntItem> cmntlist;
    public Map<String, CmntDetail> cmntstore;
    public int comment_num;
    public int join_num;
    public String token;
    public int view_num;
    public int page;
    public String sid;
    public int dig_num;
    public int fav_num;
}
