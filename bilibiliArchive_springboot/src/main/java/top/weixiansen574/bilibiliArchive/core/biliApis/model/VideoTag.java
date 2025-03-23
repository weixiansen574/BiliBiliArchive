package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import java.util.Objects;

public class VideoTag {
    public Long tag_id;
    public String tag_name;
    public Integer type;
    public Long ctime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoTag videoTag = (VideoTag) o;
        return Objects.equals(tag_id, videoTag.tag_id) && Objects.equals(tag_name, videoTag.tag_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag_id, tag_name);
    }
}
