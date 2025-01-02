package top.weixiansen574.bilibiliArchive.bean.backups;

import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

public class BackupUploader {
    public Long uid;
    public String name;
    public String desc;
    public String avatarUrl;
    public Long backupStartTime;
    public Long backupUserId;
    public Boolean videoBackupEnable;
    public Integer videoBackupConfigId;
    public Boolean dynamicBackupEnable;
    public String dynamicBackupConfig;
    public Boolean articleBackupEnable;
    public String articleBackupConfig;
    public Boolean liveRecordingEnable;
    public String liveRecordingConfig;

    public VideoBackupConfig videoBackupConfig;

    // 构造函数（可选）
    public BackupUploader() {}

    // 如果需要，可以添加一个包含所有字段的构造函数
    public BackupUploader(Long uid, String name, String desc, String avatarUrl,
                          Long backupStartTime, Long backupUserId, Boolean videoBackupEnable, Integer videoBackupConfigId,
                          Boolean dynamicBackupEnable, String dynamicBackupConfig, Boolean articleBackupEnable, String articleBackupConfig,
                          Boolean liveRecordingEnable, String liveRecordingConfig) {
        this.uid = uid;
        this.name = name;
        this.desc = desc;
        this.avatarUrl = avatarUrl;
        this.backupStartTime = backupStartTime;
        this.backupUserId = backupUserId;
        this.videoBackupEnable = videoBackupEnable;
        this.videoBackupConfigId = videoBackupConfigId;
        this.dynamicBackupEnable = dynamicBackupEnable;
        this.dynamicBackupConfig = dynamicBackupConfig;
        this.articleBackupEnable = articleBackupEnable;
        this.articleBackupConfig = articleBackupConfig;
        this.liveRecordingEnable = liveRecordingEnable;
        this.liveRecordingConfig = liveRecordingConfig;
    }

    public boolean check(){
        return MiscUtils.notNulls(uid,name,backupUserId,videoBackupEnable,videoBackupConfigId,
                dynamicBackupEnable,articleBackupEnable,liveRecordingEnable);
    }
}
