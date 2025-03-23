package top.weixiansen574.bilibiliArchive.bean;

import top.weixiansen574.bilibiliArchive.bean.list.VideoTagList;

public class VideoMetadataLog {
    public VideoMetadataLog(String title, String desc, String coverUrl, VideoTagList tags, long saveTime) {
        this.title = title;
        this.desc = desc;
        this.coverUrl = coverUrl;
        this.tags = tags;
        this.saveTime = saveTime;
    }

    public VideoMetadataLog() {
    }

    public String title;
    public String desc;
    public String coverUrl;
    public VideoTagList tags;
    public long saveTime;
}
