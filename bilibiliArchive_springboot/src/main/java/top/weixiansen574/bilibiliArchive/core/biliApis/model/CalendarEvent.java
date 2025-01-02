package top.weixiansen574.bilibiliArchive.core.biliApis.model;

public class CalendarEvent {

    public CalendarAttrs calendar_attrs;
    public Events events;
    public FlagCompletion flag_completion;
    public Pfs pfs;

    public static class AttrItem {
        public String name;
        public String icon;
        public Integer rank;
    }

    public static class CalendarAttrs {
    }

    public static class Events {
    }

    public static class FlagCompletion {
    }

    public static class Profile {
        public Long mid;
        public String name;
        public String sex;
        public String face;
        public String sign;
        public Integer rank;
        public Integer level;
        public Long jointime;
        public Integer moral;
        public Integer silence;
        public Integer email_status;
        public Integer tel_status;
        public Integer identification;
        public Vip vip;
        public Pendant pendant;
        public Nameplate nameplate;
        public Official official;
        public Long birthday;
        public Integer is_tourist;
        public Integer is_fake_account;
        public Integer pin_prompting;
        public Integer is_deleted;
        public Integer in_reg_audit;
        public Boolean is_rip_user;
        public Profession profession;
        public Integer face_nft;
        public Integer face_nft_new;
        public Integer is_senior_member;
        public Honours honours;
        public String digital_id;
        public Integer digital_type;
        public Attestation attestation;
        public ExpertInfo expert_info;
    }


    public static class AvatarIcon {
        public Integer icon_type;
//        public IconResource icon_resource;
    }

//    public static class IconResource {
//    }

    public static class Pendant {
        public Integer pid;
        public String name;
        public String image;
        public Long expire;
        public String image_enhance;
        public String image_enhance_frame;
        public Integer n_pid;
    }

    public static class Nameplate {
        public Integer nid;
        public String name;
        public String image;
        public String image_small;
        public String level;
        public String condition;
    }

    public static class Official {
        public Integer role;
        public String title;
        public String desc;
        public Integer type;
    }

    public static class Profession {
        public Long id;
        public String name;
        public String show_name;
        public Integer is_show;
        public String category_one;
        public String realname;
        public String title;
        public String department;
        public String certificate_no;
        public Boolean certificate_show;
    }

    public static class Honours {
        public Long mid;
        public Colour colour;
        public Object tags;
        public Integer is_latest_100honour;
    }

    public static class Colour {
        public String dark;
        public String normal;
    }

    public static class Attestation {
        public Integer type;
        public CommonInfo common_info;
        public SpliceInfo splice_info;
        public String icon;
        public String desc;
    }

    public static class CommonInfo {
        public String title;
        public String prefix;
        public String prefix_title;
    }

    public static class SpliceInfo {
        public String title;
    }

    public static class ExpertInfo {
        public String title;
        public Integer state;
        public Integer type;
        public String desc;
    }

    public static class LevelInfo {
        public Integer current_level;
        public Integer current_min;
        public Integer current_exp;
        public Integer next_exp;
        public Long level_up;
    }

    public static class MaskedPrivacy {
        public Long mid;
        public String realname;
        public String identity_card;
        public String telephone;
        public String login_ip;
        public String email;
    }

    public static class UserHonourInfo {
        public Long mid;
        public Colour colour;
        public Object tags;
        public Integer is_latest_100honour;
    }

    public static class School {
        public Long school_id;
        public String name;
    }
}

