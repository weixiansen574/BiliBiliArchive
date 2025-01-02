package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import java.util.List;

public class CommentPage {

    public Page page;
    public List<BiliComment> replies;
    public List<BiliComment> top_replies;

    public static class Page {
        public int num;
        public int size;
        public int count;
        public int acount;
    }
}
