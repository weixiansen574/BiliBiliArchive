package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import java.util.List;

public class SearchResponse {

    public int next;
    public int show_column;
    //public Object[] show_module_list;
    public int in_white_key;
    public int numResults;
    public int egg_hit;
    public String rqt_type;
    //public Object exp_list;
    public String suggest_keyword;
    public List<Result> result;
    public int numPages;
    //public Object app_display_option;
    public int in_black_key;
    //public Object cost_time;
    public int is_search_page_grayed;
    public String seid;
    public int pagesize;
    //public Object top_tlist;
    public int page;
    //public Object pageinfo;

    public static class Result {
        public String result_type;
        public List<VideoItem> data;
    }

    public static class VideoItem {
        public long play;
        public long rank_score;
        public String pic;
        public String type;
        public String cover;
        public String arcrank;
        public String corner;
        public long review;
        public String cate_name;
        public String typeid;
        public long id;
        public String tag;
        public int live_status;
        public int area;
        public int enable_vt;
        public String uname;
        public long like;
        public String author;
        public String arcurl;
        public String view_type;
        public int short_id;
        public String user_cover;
        public String rec_reason;
        public String tags;
        public int is_union_video;
        public String parent_area_name;
        public String subtitle;
        public int danmaku;
        public int style;
        public long aid;
        public int is_pay;
        public int vt;
        public String desc;
        public int favorites;
        public int spread_id;
        public String bvid;
        public String upic;
        public int is_charge_video;
        public long mid;
        public String description;
        public int rank_index;
        public boolean badgepay;
        public String title;
        public String duration;
        public String vt_display;
        public long uid;
        public long senddate;
        public int video_review;
        public int rank_offset;
        public int parent_area_id;
        public long pubdate;
        public String live_time;
        public int release_status;
        public String url;
        public long roomid;
        public int is_live_room_inline;
        public String uface;
        public int online;
        public int is_intervene;
        public String typename;
        public String episode_count_text;
    }
}
