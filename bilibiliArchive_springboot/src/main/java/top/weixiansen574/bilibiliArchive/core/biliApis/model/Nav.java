package top.weixiansen574.bilibiliArchive.core.biliApis.model;

public class Nav {

    public boolean isLogin;
    public int email_verified;
    public String face;
    public int face_nft;
    public int face_nft_type;
    public LevelInfo level_info;
    public long mid;
    public int mobile_verified;
    public double money;
    public int moral;
    public Official official;
    public OfficialVerify officialVerify;
    public Pendant pendant;
    public int scores;
    public String uname;
    public long vipDueDate;
    public int vipStatus;
    public int vipType;
    public int vip_pay_type;
    public int vip_theme_type;
    public VipLabel vip_label;
    public int vip_avatar_subscript;
    public String vip_nickname_color;
    public Vip vip;
    public Wallet wallet;
    public boolean has_shop;
    public String shop_url;
    public int allowance_count;
    public int answer_status;
    public int is_senior_member;
    public WbiImg wbi_img;
    public boolean is_jury;
    public Object name_render;

    public static class LevelInfo {
        public int current_level;
        public int current_min;
        public int current_exp;
        public String next_exp;
    }

    public static class Official {
        public int role;
        public String title;
        public String desc;
        public int type;
    }

    public static class OfficialVerify {
        public int type;
        public String desc;
    }

    public static class Pendant {
        public int pid;
        public String name;
        public String image;
        public long expire;
        public String image_enhance;
        public String image_enhance_frame;
        public int n_pid;
    }

    public static class VipLabel {
        public String path;
        public String text;
        public String label_theme;
        public String text_color;
        public int bg_style;
        public String bg_color;
        public String border_color;
        public boolean use_img_label;
        public String img_label_uri_hans;
        public String img_label_uri_hant;
        public String img_label_uri_hans_static;
        public String img_label_uri_hant_static;
    }

    public static class Wallet {
        public long mid;
        public double bcoin_balance;
        public double coupon_balance;
        public long coupon_due_time;
    }

    public static class WbiImg {
        public String img_url;
        public String sub_url;
    }
}
