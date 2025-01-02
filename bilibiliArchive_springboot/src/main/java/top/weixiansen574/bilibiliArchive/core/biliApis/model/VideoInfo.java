package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import com.alibaba.fastjson.JSON;

import java.util.List;

public class VideoInfo {
  /*  public static final int VIDEO_FAILED_CODE = -404;
    public static final int VIDEO_FAILED_CODE_UP_DELETED = 62002;
    public static final int VIDEO_IS_PRIVATE = 62012;*/

    public String bvid;
    public long aid;
    public int videos;
    public int tid;
    public String tname;
    public int copyright;
    public String pic;
    public String title;
    public long pubdate;
    public long ctime;
    public String desc;
    public int state;
    public int duration;
    public Owner owner;
    public Stat stat;
    public String dynamic;
    public long cid;
    public Dimension dimension;
    public long season_id;
    public String premiere;
    public int teenage_mode;
    public boolean is_chargeable_season;
    public boolean is_story;
    public boolean is_upower_exclusive;
    public boolean is_upower_play;
    public int enable_vt;
    public boolean no_cache;
    public List<VideoPage> pages;
    public Subtitle subtitle;
    public boolean is_season_display;
    public String like_icon;
    public boolean need_jump_bv;
    public boolean disable_show_up_info;
    public boolean is_upower_preview;

    public String getPagesJSONString(){
        return JSON.toJSONString(this.pages);
    }

    public static class Stat {
        public long aid;
        public long view;
        public long danmaku;
        public long reply;
        public long favorite;
        public long coin;
        public long share;
        public int now_rank;
        public int his_rank;
        public int like;
        public int dislike;
        public String evaluation;
        public String argue_msg;
        public int vt;
    }

    public static class Owner {
        public long mid;
        public String name;
        public String face;
    }

    public static class Subtitle {
        public boolean allow_submit;
        public List<SubtitleList> list;
    }

    public static class SubtitleList {
        public long id;
        public String lan;
        public String lan_doc;
        public boolean is_lock;
        public String subtitle_url;
        public int type;
        public String id_str;
        public int ai_type;
        public int ai_status;
        public Author author;
    }

    public static class Author {
        public long mid;
        public String name;
        public String sex;
        public String face;
        public String sign;
        public int rank;
        public int birthday;
        public int is_fake_account;
        public int is_deleted;
        public int in_reg_audit;
        public int is_senior_member;
    }

}
