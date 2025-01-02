package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import java.util.List;
import java.util.Map;

public class UploaderVideoPage {
    public ListData list;
    public Page page;
    public EpisodicButton episodic_button;
    public boolean is_risk;
    public int gaia_res_type;
    public Object gaia_data;

    public static class ListData {
        public Map<String, TlistData> tlist;
        public List<UpVideo> vlist;
        public List<Object> slist;
    }

    public static class TlistData {
        public int tid;
        public int count;
        public String name;
    }

    public static class UpVideo {
        public int comment;
        public int typeid;
        public int play;
        public String pic;
        public String subtitle;
        public String description;
        public String copyright;
        public String title;
        public int review;
        public String author;
        public long mid;
        public long created;
        public String length;
        public int video_review;
        public long aid;
        public String bvid;
        public boolean hide_click;
        public int is_pay;
        public int is_union_video;
        public int is_steins_gate;
        public int is_live_playback;
        public int is_lesson_video;
        public int is_lesson_finished;
        public String lesson_update_info;
        public String jump_url;
        public Object meta;
        public int is_avoided;
        public int season_id;
        public int attribute;
        public boolean is_charging_arc;
        public int vt;
        public int enable_vt;
        public String vt_display;
        public int playback_position;
        public boolean is_self_view;
    }

    public static class Page {
        public int pn;
        public int ps;
        public int count;
    }

    public static class EpisodicButton {
        public String text;
        public String uri;
    }
}
