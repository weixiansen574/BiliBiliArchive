package top.weixiansen574.bilibiliArchive.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.weixiansen574.bilibiliArchive.bean.ArchiveComment;
import top.weixiansen574.bilibiliArchive.mapper.comment.CommentMapper;

@Service
public class CommentService {

    CommentMapper commentMapper;

    @Autowired
    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Transactional
    public void test(){

    }
}
