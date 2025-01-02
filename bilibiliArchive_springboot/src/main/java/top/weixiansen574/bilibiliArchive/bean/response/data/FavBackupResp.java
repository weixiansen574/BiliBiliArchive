package top.weixiansen574.bilibiliArchive.bean.response.data;

import top.weixiansen574.bilibiliArchive.bean.backups.BackupFav;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.FavoriteVideoInfo;

public class FavBackupResp {
    public BackupFav backup;
    public FavoriteVideoInfo firstVideo;
    public int videoCount;

    public FavBackupResp(BackupFav backup, FavoriteVideoInfo firstVideo, int videoCount) {
        this.backup = backup;
        this.firstVideo = firstVideo;
        this.videoCount = videoCount;
    }

    public FavBackupResp() {
    }
}
