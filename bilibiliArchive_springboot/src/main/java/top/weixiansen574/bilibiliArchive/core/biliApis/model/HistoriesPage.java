package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import java.util.List;

public class HistoriesPage {

    public Cursor cursor;
    public List<HistoryItem> list;
    public static class Cursor {
        public long max;
        public long view_at;
        public String business;
        public int ps;
    }

    public static class History {

        public long oid;
        public int epid;
        public String bvid;
        public int page;
        public long cid;
        public String part;
        public String business;
        public int dt;
    }

    public static class HistoryItem {
        public String title;
        public String long_title;
        public String cover;
        public String covers;
        public String uri;
        public History history;
        public int videos;
        public String author_name;
        public String author_face;
        public long author_mid;
        public long view_at;
        public int progress;
        public String badge;
        public String show_title;
        public int duration;
        public String current;
        public int total;
        public String new_desc;
        public int is_finish;
        public int is_fav;
        public long kid;
        public String tag_name;
        public int live_status;
    }
}
