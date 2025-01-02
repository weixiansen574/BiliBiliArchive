package top.weixiansen574.bilibiliArchive.bean.list;

import org.jetbrains.annotations.NotNull;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoTag;

import java.util.ArrayList;
import java.util.Collection;

public class VideoTagList extends ArrayList<VideoTag> {
    public VideoTagList(@NotNull Collection<? extends VideoTag> c) {
        super(c);
    }

    public VideoTagList() {
    }
}
