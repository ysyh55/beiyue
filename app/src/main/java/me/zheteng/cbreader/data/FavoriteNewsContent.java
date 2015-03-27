package me.zheteng.cbreader.data;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table FAVORITE_NEWS_CONTENT.
 */
public class FavoriteNewsContent {

    private Long id;
    /** Not-null value. */
    private String sid;
    private String catid;
    private String topic;
    private String aid;
    private String title;
    private String keywords;
    private String hometext;
    private String listorder;
    private String comments;
    private String counter;
    private String mview;
    private String collectnum;
    private String good;
    private String bad;
    private String score;
    private String ratings;
    private String score_story;
    private String ratings_story;
    private String pollid;
    private String queueid;
    private String ifcom;
    private String ishome;
    private String elite;
    private String status;
    private String inputtime;
    private String updatetime;
    private String thumb;
    private String source;
    private String data_id;
    private String bodytext;
    private String relation;
    private String shorttitle;
    private String brief;
    private String time;

    public FavoriteNewsContent() {
    }

    public FavoriteNewsContent(Long id) {
        this.id = id;
    }

    public FavoriteNewsContent(Long id, String sid, String catid, String topic, String aid, String title, String keywords, String hometext, String listorder, String comments, String counter, String mview, String collectnum, String good, String bad, String score, String ratings, String score_story, String ratings_story, String pollid, String queueid, String ifcom, String ishome, String elite, String status, String inputtime, String updatetime, String thumb, String source, String data_id, String bodytext, String relation, String shorttitle, String brief, String time) {
        this.id = id;
        this.sid = sid;
        this.catid = catid;
        this.topic = topic;
        this.aid = aid;
        this.title = title;
        this.keywords = keywords;
        this.hometext = hometext;
        this.listorder = listorder;
        this.comments = comments;
        this.counter = counter;
        this.mview = mview;
        this.collectnum = collectnum;
        this.good = good;
        this.bad = bad;
        this.score = score;
        this.ratings = ratings;
        this.score_story = score_story;
        this.ratings_story = ratings_story;
        this.pollid = pollid;
        this.queueid = queueid;
        this.ifcom = ifcom;
        this.ishome = ishome;
        this.elite = elite;
        this.status = status;
        this.inputtime = inputtime;
        this.updatetime = updatetime;
        this.thumb = thumb;
        this.source = source;
        this.data_id = data_id;
        this.bodytext = bodytext;
        this.relation = relation;
        this.shorttitle = shorttitle;
        this.brief = brief;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getSid() {
        return sid;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getCatid() {
        return catid;
    }

    public void setCatid(String catid) {
        this.catid = catid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getHometext() {
        return hometext;
    }

    public void setHometext(String hometext) {
        this.hometext = hometext;
    }

    public String getListorder() {
        return listorder;
    }

    public void setListorder(String listorder) {
        this.listorder = listorder;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCounter() {
        return counter;
    }

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public String getMview() {
        return mview;
    }

    public void setMview(String mview) {
        this.mview = mview;
    }

    public String getCollectnum() {
        return collectnum;
    }

    public void setCollectnum(String collectnum) {
        this.collectnum = collectnum;
    }

    public String getGood() {
        return good;
    }

    public void setGood(String good) {
        this.good = good;
    }

    public String getBad() {
        return bad;
    }

    public void setBad(String bad) {
        this.bad = bad;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getScore_story() {
        return score_story;
    }

    public void setScore_story(String score_story) {
        this.score_story = score_story;
    }

    public String getRatings_story() {
        return ratings_story;
    }

    public void setRatings_story(String ratings_story) {
        this.ratings_story = ratings_story;
    }

    public String getPollid() {
        return pollid;
    }

    public void setPollid(String pollid) {
        this.pollid = pollid;
    }

    public String getQueueid() {
        return queueid;
    }

    public void setQueueid(String queueid) {
        this.queueid = queueid;
    }

    public String getIfcom() {
        return ifcom;
    }

    public void setIfcom(String ifcom) {
        this.ifcom = ifcom;
    }

    public String getIshome() {
        return ishome;
    }

    public void setIshome(String ishome) {
        this.ishome = ishome;
    }

    public String getElite() {
        return elite;
    }

    public void setElite(String elite) {
        this.elite = elite;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInputtime() {
        return inputtime;
    }

    public void setInputtime(String inputtime) {
        this.inputtime = inputtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getData_id() {
        return data_id;
    }

    public void setData_id(String data_id) {
        this.data_id = data_id;
    }

    public String getBodytext() {
        return bodytext;
    }

    public void setBodytext(String bodytext) {
        this.bodytext = bodytext;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getShorttitle() {
        return shorttitle;
    }

    public void setShorttitle(String shorttitle) {
        this.shorttitle = shorttitle;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
