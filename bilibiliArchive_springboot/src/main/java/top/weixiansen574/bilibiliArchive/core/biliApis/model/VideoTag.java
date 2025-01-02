package top.weixiansen574.bilibiliArchive.core.biliApis.model;

public class VideoTag {
    public Long tag_id;
    public String tag_name;
    public String cover;
    public String head_cover;
    public String content;
    public String short_content;
    public Integer type;
    public Integer state;
    public Long ctime;
    public Count count;
    public Integer is_atten;
    public Integer likes;
    public Integer hates;
    public Integer attribute;
    public Integer liked;
    public Integer hated;
    public Integer extra_attr;
    public static class Count {
        public Integer view;
        public Integer use;
        public Integer atten;
    }
}
