package top.weixiansen574.bilibiliArchive.bean.list;

import org.jetbrains.annotations.NotNull;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;

import java.util.ArrayList;
import java.util.Collection;

public class StaffList extends ArrayList<VideoInfo.Staff> {
    public StaffList() {
    }
    public StaffList(@NotNull Collection<? extends VideoInfo.Staff> c) {
        super(c);
    }
}
