package top.weixiansen574.bilibiliArchive.bean.videoinfo;

import top.weixiansen574.bilibiliArchive.core.biliApis.model.SubtitleInfo;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoPage;

import java.util.List;

public class DownloadedVideoPage extends VideoPage {
    public DownloadedVideoPage(VideoPage page, int codecId, int qn, int width, int height) {
        this.cid = page.cid;
        this.page = page.page;
        this.from = page.from;
        this.part = page.part;
        this.duration = page.duration;
        this.vid = page.vid;
        this.weblink = page.weblink;
        this.dimension = page.dimension;
        this.first_frame = page.first_frame;
        this.codecId = codecId;
        this.qn = qn;
        this.width = width;
        this.height = height;
    }

    public DownloadedVideoPage() {
    }

    public int codecId;
    public int qn;
    public int width;
    public int height;
    public List<SubtitleInfo> subtitles;
}
