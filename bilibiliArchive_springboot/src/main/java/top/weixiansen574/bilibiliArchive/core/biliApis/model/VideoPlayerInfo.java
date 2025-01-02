package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import java.util.List;

public class VideoPlayerInfo {
    public long aid;
    public String bvid;
    public boolean allow_bp;
    public boolean no_share;
    public long cid;
    public int max_limit;
    public int page_no;
    public boolean has_next;
    public IpInfo ip_info;
    public long login_mid;
    public String login_mid_hash;
    public boolean is_owner;
    public String name;
    public String permission;
    public LevelInfo level_info;
    public Vip vip;
    public int answer_status;
    public int block_time;
    public String role;
    public int last_play_time;
    public long last_play_cid;
    public long now_time;
    public int online_count;
    public boolean need_login_subtitle;
    public Subtitles subtitle;
    public String preview_toast;
    public Options options;
    public OnlineSwitch online_switch;
    public Fawkes fawkes;
    public ShowSwitch show_switch;
    public boolean toast_block;
    public boolean is_upower_exclusive;
    public boolean is_upower_play;
    public boolean is_ugc_pay_preview;
    public ElecHighLevel elec_high_level;
    public boolean disable_show_up_info;

    public static class IpInfo {
        public String ip;
        public String zone_ip;
        public int zone_id;
        public String country;
        public String province;
        public String city;
    }


    public static class Subtitles {
        public boolean allow_submit;
        public String lan;
        public String lan_doc;
        public List<SubtitleInfo> subtitles;
    }

    public static class Options {
        public boolean is_360;
        public boolean without_vip;
    }

    public static class OnlineSwitch {
        public String enable_gray_dash_playback;
        public String new_broadcast;
        public String realtime_dm;
        public String subtitle_submit_switch;
    }

    public static class Fawkes {
        public int config_version;
        public int ff_version;
    }

    public static class ShowSwitch {
        public boolean long_progress;
    }

    public static class ElecHighLevel {
        public int privilege_type;
        public String title;
        public String sub_title;
        public boolean show_button;
        public String button_text;
        public String jump_url;
        public String intro;
    }
}
