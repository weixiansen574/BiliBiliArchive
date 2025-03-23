package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.weixiansen574.bilibiliArchive.bean.*;
import top.weixiansen574.bilibiliArchive.bean.response.BaseResponse;
import top.weixiansen574.bilibiliArchive.bean.response.ResponseCode;
import top.weixiansen574.bilibiliArchive.bean.response.data.CommentPage;
import top.weixiansen574.bilibiliArchive.bean.response.data.CommentReplyPage;
import top.weixiansen574.bilibiliArchive.mapper.comment.AvatarMapper;
import top.weixiansen574.bilibiliArchive.mapper.comment.CommentMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    public static final int COMMENT_SORT_BY_TIME = 0;
    public static final int COMMENT_SORT_BY_LIKE = 1;
    public static final int COMMENT_SORT_BY_REPLY = 2;

    public CommentMapper commentMapper;
    public AvatarMapper avatarMapper;

    @Autowired
    public CommentController(CommentMapper commentMapper, AvatarMapper avatarMapper) {
        this.commentMapper = commentMapper;
        this.avatarMapper = avatarMapper;
    }

    @GetMapping("avatars/{fileName}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String fileName){
        if (avatarMapper.checkExists(fileName)) {
            byte[] avatarData = avatarMapper.selectByName(fileName).data;
            if (avatarData == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/avif")
                    .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                    .body(avatarData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("main")
    public BaseResponse<CommentPage> getComments
            (@RequestParam int type, @RequestParam long oid, @RequestParam(required = false, defaultValue = "1") int sort,
             @RequestParam(required = false,defaultValue = "20") int ps,
             @RequestParam(required = false, defaultValue = "1") int pn) {

        if (pn <= 0) {
            return BaseResponse.illegalRequest("pn 参数不合法");
        }
        int offset = (pn - 1) * 20;
        CommentPage.PageInfo pageInfo = new CommentPage.PageInfo();

        pageInfo.rootCount = commentMapper.selectRootCommentCountForOid(oid, type);
        pageInfo.allCount = commentMapper.selectCommentAllCountForOid(oid, type);

        String orderByField;

        switch (sort) {
            case COMMENT_SORT_BY_LIKE -> orderByField = "like";
            case COMMENT_SORT_BY_TIME -> orderByField = "ctime";
            case COMMENT_SORT_BY_REPLY -> orderByField = "reply";
            default -> {
                return BaseResponse.illegalRequest("sort 参数不合法");
            }
        }

        List<ArchiveComment> comments = commentMapper.rangeQueryRootComments(oid, type, orderByField,ps, offset);


        for (ArchiveComment comment : comments) {
            comment.replyCount = commentMapper.selectCommentReplyCount(comment.rpid);
            if (comment.replyCount != 0){
                comment.previewReplies = commentMapper.selectRepliesWithPagination(comment.rpid,3,0);
            }
        }
        return BaseResponse.ok(new CommentPage(comments, pageInfo));
    }

    @GetMapping("reply")
    public BaseResponse<CommentReplyPage> getReplies(@RequestParam long root, @RequestParam(required = false, defaultValue = "1") int sort,
                                                     @RequestParam(required = false,defaultValue = "20") int ps,
                                                     @RequestParam(required = false, defaultValue = "1") int pn){
        ArchiveComment rootComment = commentMapper.selectByRpid(root);
        if (rootComment == null){
            return BaseResponse.error(ResponseCode.NOT_FOUND, "根评论不存在");
        }
        CommentReplyPage page = new CommentReplyPage();
        page.root = rootComment;
        page.replyCount = commentMapper.selectCommentReplyCount(root);
        if (ps == -1){
            page.replies =  commentMapper.selectAllReplies(root);
        } else {
            int offset = (pn - 1) * 20;
            page.replies = commentMapper.selectRepliesWithPagination(root,ps,offset);
        }
        return BaseResponse.ok(page);
    }
}
