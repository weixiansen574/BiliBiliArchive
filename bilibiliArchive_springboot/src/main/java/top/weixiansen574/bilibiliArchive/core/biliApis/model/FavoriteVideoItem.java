package top.weixiansen574.bilibiliArchive.core.biliApis.model;

public class FavoriteVideoItem {
    public long id;
    public int type;
    public String title;
    public String cover;
    public String intro;
    public int page;
    public int duration;
    public Upper upper;
    public int attr;
    public Cnt_info cnt_info;
    public String link;
    public long ctime;
    public long pubtime;
    public long fav_time;
    public String bv_id;
    public String bvid;
    public String season;
    public String ogv;
    public Ugc ugc;

    public static class Upper {
        public long mid;
        public String name;
        public String face;
    }

    public static class Cnt_info {
        public int collect;
        public int play;
        public int danmaku;
        public int vt;
        public int play_switch;
        public int reply;
        public String view_text_1;
    }

    public static class Ugc {
        public long first_cid;
    }

}
