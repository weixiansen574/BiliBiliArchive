package top.weixiansen574.bilibiliArchive.core.biliApis.model;

public class FavoriteInfoResponse {
    public long id;
    public long fid;
    public long mid;
    public int attr;
    public String title;
    public String cover;
    public Upper upper;
    public int cover_type;
    public CntInfo cnt_info;
    public int type;
    public String intro;
    public int ctime;
    public int mtime;
    public int state;
    public int fav_state;
    public int like_state;
    public int media_count;
    public boolean is_top;

    public static class Upper {
        public long mid;
        public String name;
        public String face;
        public boolean followed;
        public int vip_type;
        public int vip_statue;
    }

    public static class CntInfo {
        public int collect;
        public int play;
        public int thumb_up;
        public int share;
    }
}
