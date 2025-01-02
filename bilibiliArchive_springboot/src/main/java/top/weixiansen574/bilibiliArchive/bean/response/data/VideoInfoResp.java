package top.weixiansen574.bilibiliArchive.bean.response.data;

import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;

public class VideoInfoResp {
    public ArchiveVideoInfo videoInfo;
    public int allCommentCount;
    public int rootCommentCount;
    public long diskUsage;

    public VideoInfoResp(ArchiveVideoInfo videoInfo, int allCommentCount,int rootCommentCount,long diskUsage) {
        this.videoInfo = videoInfo;
        this.allCommentCount = allCommentCount;
        this.rootCommentCount = rootCommentCount;
        this.diskUsage = diskUsage;
    }

    public VideoInfoResp() {
    }

}
