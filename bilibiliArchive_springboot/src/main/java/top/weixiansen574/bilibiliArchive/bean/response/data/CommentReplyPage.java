package top.weixiansen574.bilibiliArchive.bean.response.data;

import top.weixiansen574.bilibiliArchive.bean.ArchiveComment;

import java.util.List;

public class CommentReplyPage {
    public ArchiveComment root;
    public List<ArchiveComment> replies;
    public int replyCount;

    public CommentReplyPage(ArchiveComment root, List<ArchiveComment> replies, int replyCount) {
        this.root = root;
        this.replies = replies;
        this.replyCount = replyCount;
    }

    public CommentReplyPage() {
    }


}
