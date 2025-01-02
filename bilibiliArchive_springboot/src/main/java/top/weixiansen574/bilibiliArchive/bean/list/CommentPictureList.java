package top.weixiansen574.bilibiliArchive.bean.list;

import org.jetbrains.annotations.NotNull;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.BiliComment;

import java.util.ArrayList;
import java.util.Collection;

public class CommentPictureList extends ArrayList<BiliComment.Pictures> {
    public CommentPictureList(@NotNull Collection<? extends BiliComment.Pictures> c) {
        super(c);
    }
    public CommentPictureList() {
    }
}
