package top.weixiansen574.bilibiliArchive.bean;

import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.config.VideoDownloadConfig;
import top.weixiansen574.bilibiliArchive.core.biliApis.VideoQualityInfo;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

public class VideoUpdateRequest implements VideoQualityInfo {
    public String bvid;
    public Long uid;
    public Boolean updateVideoAndDanmaku;
    public VideoDownloadConfig video;
    public CommentDownloadConfig commentConfig;


    public boolean check() {
        if (!MiscUtils.notNulls(bvid,uid, updateVideoAndDanmaku, video)) {
            return false;
        }
        if (!video.check()){
            return false;
        }
        return commentConfig == null || commentConfig.check();
    }
}
