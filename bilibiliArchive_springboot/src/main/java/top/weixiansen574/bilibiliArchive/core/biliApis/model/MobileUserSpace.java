package top.weixiansen574.bilibiliArchive.core.biliApis.model;

public class MobileUserSpace {
    public int relation;
    public int guest_relation;
    public int medal;
    public String default_tab;
    public boolean is_params;
    public Setting setting;
    public Card card;
    public Live live;

    public static class Setting {
        public int channel;
        public int fav_video;
        public int coins_video;
        public int likes_video;
        public int bangumi;
        public int played_game;
        public int groups;
        public int comic;
        public int bbq;
        public int dress_up;
        public int disable_following;
        public int live_playback;
        public int close_space_medal;
        public int only_show_wearing;
        public int disable_show_school;
        public int disable_show_nft;
        public int disable_show_fans;
        public int charge_video;
        public int lesson_video;
    }

    public static class Card {
        public Object avatar;
        public String mid;
        public String name;
        public boolean approve;
        public String rank;
        public String face;
        public String DisplayRank;
        public int regtime;
        public int spacesta;
        public String birthday;
        public String place;
        public String description;
        public int article;
        public Object attentions;
        public int fans;
        public int friend;
        public int attention;
        public String sign;
        public LevelInfo level_info;
        public Pendant pendant;
        public Nameplate nameplate;
        public OfficialVerify official_verify;
        public ProfessionVerify profession_verify;
        public Vip2 vip;
        public int silence;
        public int end_time;
        public String silence_url;
        public Likes likes;
        public Achieve achieve;
        public Object pr_info;
        public Relation relation;
        public int is_deleted;
        public Honours honours;
        public Object profession;
        public Object school;
        public Object space_tag;
        public int face_nft_new;
        public boolean has_face_nft;
        public String nft_id;
        public Object nft_face_icon;
        public String digital_id;
        public int digital_type;
        public boolean has_digital_asset;


        public static class Pendant {
            public int pid;
            public String name;
            public String image;
            public int expire;
            public String image_enhance;
            public String image_enhance_frame;
            public int n_pid;
        }

        public static class Nameplate {
            public int nid;
            public String name;
            public String image;
            public String image_small;
            public String level;
            public String condition;
        }

        public static class OfficialVerify {
            public int type;
            public String desc;
            public int role;
            public String title;
            public String icon;
            public String splice_title;
        }

        public static class ProfessionVerify {
            public String icon;
            public String show_desc;
        }


        public static class Likes {
            public int like_num;
            public String skr_tip;
        }

        public static class Achieve {
            public boolean is_default;
            public String image;
            public String achieve_url;
        }

        public static class Relation {
            public int status;
        }

        public static class Honours {
            public Colour colour;
            public Object tags;

            public static class Colour {
                public String dark;
                public String normal;
            }
        }
    }

    public static class Live {
        public int roomStatus;
        public int roundStatus;
        public int liveStatus;
        public String url;
        public String title;
        public String cover;
        public int online;
        public int roomid;
        public int broadcast_type;
        public int online_hidden;
        public String link;
    }
}
