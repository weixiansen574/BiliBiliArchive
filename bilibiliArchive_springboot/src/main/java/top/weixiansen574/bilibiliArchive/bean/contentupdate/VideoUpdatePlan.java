package top.weixiansen574.bilibiliArchive.bean.contentupdate;

import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;

public class VideoUpdatePlan extends CommentUpdatePlan {
    public String bvid;
    public Long avid;
    public String title;

    public Boolean updateVideoAndDanmaku;

    public Integer videoQuality;
    public Integer videoCodecId;


    @Override
    public String toString() {
        return "VideoUpdatePlan{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", uid=" + uid +
                ", comment_config_1='" + commentConfig1 + '\'' +
                ", comment_config_2='" + commentConfig2 + '\'' +
                ", bvid='" + bvid + '\'' +
                ", avid=" + avid +
                ", title='" + title + '\'' +
                ", update_video_and_danmaku=" + updateVideoAndDanmaku +
                ", video_quality=" + videoQuality +
                ", video_codec_id=" + videoCodecId +
                '}';
    }
}
