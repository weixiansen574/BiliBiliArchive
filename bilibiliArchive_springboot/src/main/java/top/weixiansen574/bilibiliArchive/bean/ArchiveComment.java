package top.weixiansen574.bilibiliArchive.bean;

import com.alibaba.fastjson2.JSON;
import top.weixiansen574.bilibiliArchive.bean.list.CommentPictureList;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.BiliComment;

import java.util.List;

public class ArchiveComment {
    public long rpid;
    public long oid;
    public int type;
    public long mid;
    public long root;
    public long parent;
    public String uname;
    public int currentLevel;
    public String location;
    public String message;
    public int like;
    public long ctime;
    public CommentPictureList pictures;
    public String avatarUrl;
    public int vipType;
    public Integer floor;
    public int replyCount;
    public List<ArchiveComment> previewReplies;

    public ArchiveComment() {
    }

    public ArchiveComment(BiliComment source, Integer floor){
        this.rpid = source.rpid;
        this.oid = source.oid;
        this.type = source.type;
        this.root = source.root;
        this.parent = source.parent;
        this.ctime = source.ctime;
        this.like = source.like;
        this.message = source.content.message;
        this.mid = source.member.mid;
        this.uname = source.member.uname;
        this.currentLevel = source.member.level_info.current_level;
        this.location = source.reply_control.location;
        this.avatarUrl = source.member.avatar;
        this.pictures = source.content.pictures != null ? new CommentPictureList(source.content.pictures) : null;//存档图片URL
        this.vipType = source.member.vip.vipType;
        this.floor = floor;
    }
}
