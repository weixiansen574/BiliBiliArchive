package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import java.util.List;

public class Emotes {
    public List<Package> packages;

    public static class Package{
        public long id;
        public String text;
        public List<EmoteInfo> emote;
    }

    public static class EmoteInfo{
        public int id;
        public int package_id;
        public String text;
        public String url;
        public long mtime;
        public int type;
        public int attr;
        public Meta meta;
        public Flags flags;
        public Object activity;

        public static class Meta {
            public int size;
            public String[] suggest;
            public String alias;
        }

        public static class Flags {
            public boolean unlocked;
        }
    }
}
