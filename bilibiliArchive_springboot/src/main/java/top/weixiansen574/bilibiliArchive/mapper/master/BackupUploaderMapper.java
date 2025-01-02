package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupHistory;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupUploader;

import java.util.List;

@Repository
public interface BackupUploaderMapper {
    @Insert("""
            INSERT INTO backups_uploader (uid, name, `desc`, avatar_url,
            backup_start_time, backup_user_id, video_backup_enable, video_backup_config_id,
            dynamic_backup_enable, dynamic_backup_config, article_backup_enable, article_backup_config,
            live_recording_enable, live_recording_config)
            VALUES (#{uid}, #{name}, #{desc}, #{avatarUrl},
            #{backupStartTime}, #{backupUserId}, #{videoBackupEnable}, #{videoBackupConfigId},
            #{dynamicBackupEnable}, #{dynamicBackupConfig}, #{articleBackupEnable}, #{articleBackupConfig},
            #{liveRecordingEnable}, #{liveRecordingConfig})
            """)
    void insert(BackupUploader backupUploader);

    @Delete("DELETE FROM backups_uploader WHERE uid = #{uid}")
    void deleteByUpUid(long uid);

    @Update("""
        UPDATE backups_uploader
        SET name = #{name},
            `desc` = #{desc},
            avatar_url = #{avatarUrl},
            backup_start_time = #{backupStartTime},
            backup_user_id = #{backupUserId},
            video_backup_enable = #{videoBackupEnable},
            video_backup_config_id = #{videoBackupConfigId},
            dynamic_backup_enable = #{dynamicBackupEnable},
            dynamic_backup_config = #{dynamicBackupConfig},
            article_backup_enable = #{articleBackupEnable},
            article_backup_config = #{articleBackupConfig},
            live_recording_enable = #{liveRecordingEnable},
            live_recording_config = #{liveRecordingConfig}
        WHERE uid = #{uid}
        """)
    void update(BackupUploader uploaderBackupInfo);

    @Select("SELECT * FROM backups_uploader WHERE backup_user_id = #{backupUserId}")
    List<BackupUploader> selectByBackupUserId(long backupUserId);

    @Select("SELECT * FROM backups_uploader WHERE uid = #{uid}")
    BackupUploader selectByUpUid(long uid);

    @Select("select * from backups_uploader where video_backup_enable = 1")
    @Results({
            @Result(column = "video_backup_config_id", property = "videoBackupConfigId"),
            @Result(column = "video_backup_config_id", property = "videoBackupConfig",
                    one = @One(select = "top.weixiansen574.bilibiliArchive.mapper.master.VideoBackupConfigMapper.selectById"))
    })
    List<BackupUploader> selectAllVideoEnabled();

    @Select("select COUNT(1) from backups_uploader where uid = #{uid}")
    boolean checkExists(Long uid);

    @Select("select * from backups_uploader where video_backup_config_id = #{id}")
    List<BackupUploader> selectByVideoBackupConfigId(int id);
}
