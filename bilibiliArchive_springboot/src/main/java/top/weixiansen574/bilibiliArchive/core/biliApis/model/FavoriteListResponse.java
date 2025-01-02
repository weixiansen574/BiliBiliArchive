package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import java.util.List;

public class FavoriteListResponse {
    public int count;
    public List<Info> list;
    public Object season; // Assuming 'season' can be null or some other type, using Object for simplicity

    public static class Info {
        public long id;
        public long fid;
        public long mid;
        public int attr;
        public String title;
        public int fav_state;
        public int media_count;
    }
}
