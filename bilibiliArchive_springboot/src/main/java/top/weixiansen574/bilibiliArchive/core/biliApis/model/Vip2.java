package top.weixiansen574.bilibiliArchive.core.biliApis.model;

public class Vip2 {
    public int vipType;
    public long vipDueDate;
    public String dueRemark;
    public int accessStatus;
    public int vipStatus;
    public String vipStatusWarn;
    public int themeType;
    public Label label;
    public int avatarSubscript;
    public String nicknameColor;

    public static class Label {
        public String path;
        public String text;
        public String labelTheme;
        public String textColor;
        public int bgStyle;
        public String bgColor;
        public String borderColor;
        public boolean useImgLabel;
        public String imgLabelUriHans;
        public String imgLabelUriHant;
        public String imgLabelUriHansStatic;
        public String imgLabelUriHantStatic;
    }
}