package top.weixiansen574.bilibiliArchive.core.biliApis.model;


import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

import java.util.List;

public class BiliComment {
    public long rpid;
    public long oid;
    public int type;
    public Member member;
    public long root;
    public long parent;
    public Content content;
    public Reply_control reply_control;
    public long ctime;
    public int like;
    public List<BiliComment> replies;
    public int rcount;

    public BiliComment(){}


    public String[] getPictureUrls(){
        if (content.pictures != null && content.pictures.size() > 0){
            String[] split = new String[content.pictures.size()];
            for (int i = 0; i < content.pictures.size(); i++) {
                split[i] = content.pictures.get(i).img_src;
            }
            return split;
        }
        return null;
    }

    public String getMessage(){
        return content.message;
    }

    public long getMid() {
        return member.mid;
    }

    public String getUname() {
        return member.uname;
    }

    public int getCurrent_level() {
        return member.level_info.current_level;
    }

    public String getLocation() {
        if (reply_control!=null) {
            return reply_control.location;
        } else {
            return null;
        }
    }

    public String getAvatar_url() {
        return member.avatar;
    }

/*    public ArchiveComment toArchiveComment(){
        ArchiveComment c = new ArchiveComment();
        c.rpid = rpid;
        c.oid = oid;
        c.type = type;
        c.root = root;
        c.parent = parent;
        c.ctime = ctime;
        c.like = like;
        c.message = content.message;
        c.mid = member.mid;
        c.uname = member.uname;
        c.current_level = member.level_info.current_level;
        c.location = reply_control.location;
        c.avatar_url = member.avatar;
        return c;
    }*/

    @Override
    public String toString() {
        return "Comment{" +
                "rpid=" + rpid +
                ", oid=" + oid +
                ", type=" + type +
                ", mid=" + getMid() +
                ", root=" + root +
                ", parent=" + parent +
                ", uname='" + getUname() + '\'' +
                ", currentLevel=" + getCurrent_level() +
                ", location=" + getLocation() +
                ", message='" + content.message + '\'' +
                ", like=" + like +
                ", ctime=" + MiscUtils.cnSdf.format(ctime * 1000) +
                ", userFaceUrl='" + getAvatar_url() + '\'';
    }

    public static class Content {
        public String message;
        public List<Pictures> pictures;
    }

    public static class Member{
        public long mid;
        public String uname;
        public String sex;
        public String sign;
        public String avatar;
        public Level_info level_info;
        public Vip2 vip;
    }
    public static class Level_info {
        public int current_level;
    }

    public static class Reply_control {
        public String location;
    }

    public static class Pictures{
        public String img_src;
        public int img_width;
        public int img_height;
        public double img_size;
    }

}
