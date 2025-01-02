package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupHistory;

import java.util.List;

@Repository
public interface BackupHistoryMapper {
    @Insert("""
            insert into backups_history (uid,auto_delete_method,delete_by_days,delete_by_disk_usage,delete_by_disk_usage_unit,delete_by_item_quantity,
            release_time_limit_day,uploader_black_list,video_backup_enable,video_backup_config_id)
            values(#{uid},#{autoDeleteMethod},#{deleteByDays},#{deleteByDiskUsage},#{deleteByDiskUsageUnit},#{deleteByItemQuantity},#{releaseTimeLimitDay},
            #{uploaderBlackList},#{videoBackupEnable},#{videoBackupConfigId})
            """)
    void insert(BackupHistory backupHistory);

    @Delete("DELETE FROM backups_history WHERE uid = #{uid}")
    void deleteByUid(long uid);

    @Update("""
        UPDATE backups_history SET auto_delete_method = #{autoDeleteMethod},delete_by_days = #{deleteByDays},
        delete_by_disk_usage = #{deleteByDiskUsage},delete_by_disk_usage_unit = #{deleteByDiskUsageUnit},delete_by_item_quantity = #{deleteByItemQuantity},
        release_time_limit_day = #{releaseTimeLimitDay},uploader_black_list = #{uploaderBlackList},
        video_backup_enable = #{videoBackupEnable},video_backup_config_id = #{videoBackupConfigId} where uid = #{uid}
        """)
    void update(BackupHistory backupHistory);

    @Select("SELECT * FROM backups_history WHERE uid = #{uid}")
    BackupHistory selectByUid(long uid);

    @Select("select * from backups_history where video_backup_enable = 1")
    @Results({
            @Result(column = "video_backup_config_id", property = "videoBackupConfigId"),
            @Result(column = "video_backup_config_id", property = "videoBackupConfig",
                    one = @One(select = "top.weixiansen574.bilibiliArchive.mapper.master.VideoBackupConfigMapper.selectById"))
    })
    List<BackupHistory> selectAllEnabled();

    @Select("select COUNT(1) from backups_history where uid = #{uid}")
    boolean checkExists(long uid);

    @Select("select * from backups_history where video_backup_config_id = #{id}")
    List<BackupHistory> selectByVideoBackupConfigId(int id);
}
