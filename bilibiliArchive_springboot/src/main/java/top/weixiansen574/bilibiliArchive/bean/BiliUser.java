package top.weixiansen574.bilibiliArchive.bean;

import top.weixiansen574.bilibiliArchive.core.biliApis.model.Pfs;

public class BiliUser {
    /*
    CREATE TABLE users (
    uid        INTEGER PRIMARY KEY
                       NOT NULL,
    name       TEXT    NOT NULL,
    avatar_url TEXT    NOT NULL,
    cookie     TEXT    NOT NULL,
    pfs        TEXT    NOT NULL
);
     */
    public Long uid;
    public String name;
    public String avatarUrl;
    public String cookie;
    public Pfs pfs;

    public BiliUser(String cookie,Pfs pfs){
        this.pfs = pfs;
        this.cookie = cookie;
        this.uid = pfs.profile.mid;
        this.name = pfs.profile.name;
        this.avatarUrl = pfs.profile.face;
    }

    public BiliUser() {
    }

    @Override
    public String toString() {
        return "BiliUser{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", cookie='" + cookie + '\'' +
                ", pfs='" + pfs + '\'' +
                '}';
    }

    public boolean currentTimeIsVip() {
        if (pfs.profile.vip == null){
            return false;
        }
        return pfs.profile.vip.due_date > System.currentTimeMillis();
    }
}
