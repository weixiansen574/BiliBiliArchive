package top.weixiansen574.bilibiliArchive.bean.contentupdate;

import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;
import top.weixiansen574.bilibiliArchive.core.biliApis.CommentDownloadInterface;

public class CommentUpdatePlan implements CommentDownloadInterface {
    public long id;
    public long timestamp;
    public long uid;

    public CommentDownloadConfig commentConfig1;
    public CommentDownloadConfig commentConfig2;
    public CommentDownloadConfig commentConfig3;
}
