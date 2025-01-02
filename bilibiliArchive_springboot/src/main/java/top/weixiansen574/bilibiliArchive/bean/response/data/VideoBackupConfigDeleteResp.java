package top.weixiansen574.bilibiliArchive.bean.response.data;

import top.weixiansen574.bilibiliArchive.bean.backups.BackupFav;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupHistory;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupUploader;

import java.util.List;

public class VideoBackupConfigDeleteResp {
    public List<BackupFav> backupFavList;
    public List<BackupHistory> backupHistoryList;
    public List<BackupUploader> backupUploaderList;

    public VideoBackupConfigDeleteResp(List<BackupFav> backupFavList, List<BackupHistory> backupHistoryList, List<BackupUploader> backupUploaderList) {
        this.backupFavList = backupFavList;
        this.backupHistoryList = backupHistoryList;
        this.backupUploaderList = backupUploaderList;
    }
}
