package top.weixiansen574.bilibiliArchive.bean.response.data;

import top.weixiansen574.bilibiliArchive.bean.ArchiveComment;

import java.util.List;

public class CommentPage {
    public CommentPage(List<ArchiveComment> comments, PageInfo pageInfo) {
        this.comments = comments;
        this.pageInfo = pageInfo;
    }

    public List<ArchiveComment> comments;
    public PageInfo pageInfo;

    public static class PageInfo{
        public int rootCount;
        public int allCount;
        public int pageSize = 20;
    }
}
