package top.weixiansen574.bilibiliArchive.core.biliApis.model;

public class Vip {
    public Integer type;
    public Integer status;
    public Long due_date;
    public Integer vip_pay_type;
    public Integer theme_type;
    public Label label;
    public Integer avatar_subscript;
    public String nickname_color;
    public Integer role;
    public String avatar_subscript_url;
    public Integer tv_vip_status;
    public Integer tv_vip_pay_type;
    public Long tv_due_date;
    public AvatarIcon avatar_icon;

    public static class Label {
        public String path;
        public String text;
        public String label_theme;
        public String text_color;
        public Integer bg_style;
        public String bg_color;
        public String border_color;
        public Boolean use_img_label;
        public String img_label_uri_hans;
        public String img_label_uri_hant;
        public String img_label_uri_hans_static;
        public String img_label_uri_hant_static;
    }

    public static class AvatarIcon {
        public Integer icon_type;
    }


}
