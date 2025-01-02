package top.weixiansen574.bilibiliArchive.bean.response.data;

import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;

import java.util.List;

public class VideoInfoPage {

    public PageInfo page;
    public List<ArchiveVideoInfo> videos;

    public static class PageInfo{
        public int total;
        public int pageSize;
        public int currentPage;
    }
}
