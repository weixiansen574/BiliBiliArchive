package top.weixiansen574.bilibiliArchive.bean.backups;

import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

public class BackupFav {
    public Long favId;
    public String favName;
    public Long ownerUid;
    public String ownerName;
    public Long backupUserId;
    public Boolean videoBackupEnable;
    public Integer videoBackupConfigId;

    public VideoBackupConfig videoBackupConfig;

    public BackupFav() {}

    public BackupFav(Long favId, String favName, Long ownerUid, String ownerName, Long backupUserId, Boolean videoBackupEnable, Integer videoBackupConfigId) {
        this.favId = favId;
        this.favName = favName;
        this.ownerUid = ownerUid;
        this.ownerName = ownerName;
        this.backupUserId = backupUserId;
        this.videoBackupEnable = videoBackupEnable;
        this.videoBackupConfigId = videoBackupConfigId;
    }

    public boolean check(){
        return MiscUtils.notNulls(favId,favName, ownerUid, ownerName,backupUserId,videoBackupEnable,videoBackupConfigId);
    }

    @Override
    public String toString() {
        return "BackupsFav{" +
                "favId=" + favId +
                ", favName='" + favName + '\'' +
                ", ownerUid=" + ownerUid +
                ", ownerName='" + ownerName + '\'' +
                ", backupUserId=" + backupUserId +
                ", videoBackupEnable=" + videoBackupEnable +
                ", videoBackupConfigId=" + videoBackupConfigId +
                ", videoBackupConfig=" + videoBackupConfig +
                '}';
    }
}
