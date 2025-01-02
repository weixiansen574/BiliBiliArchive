package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupFav;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupHistory;

import java.util.List;

@Repository
public interface BackupFavMapper {

    @Insert("""
            INSERT INTO backups_fav (fav_id, fav_name, owner_uid, owner_name, backup_user_id,
                                    video_backup_enable, video_backup_config_id)
            VALUES (#{favId}, #{favName}, #{ownerUid}, #{ownerName}, #{backupUserId},
                    #{videoBackupEnable}, #{videoBackupConfigId})
            """)
    void insert(BackupFav backupsFav);

    @Delete("""
            DELETE FROM backups_fav
            WHERE fav_id = #{favId}
            """)
    void deleteById(Long favId);

    @Update("""
            UPDATE backups_fav
            SET fav_name = #{favName}, owner_uid = #{ownerUid}, owner_name = #{ownerName},
                backup_user_id = #{backupUserId}, video_backup_enable = #{videoBackupEnable},
                video_backup_config_id = #{videoBackupConfigId}
            WHERE fav_id = #{favId}
            """)
    void update(BackupFav backupsFav);


    @Select("SELECT * FROM backups_fav")
    List<BackupFav> selectAll();

    @Select("select * from backups_fav where backup_user_id = #{uid}")
    List<BackupFav> selectByBackupUid(Long uid);

    @Select("select * from backups_fav where video_backup_enable = 1")
    @Results({
            @Result(column = "video_backup_config_id", property = "videoBackupConfigId"),
            @Result(column = "video_backup_config_id", property = "videoBackupConfig",
                    one = @One(select = "top.weixiansen574.bilibiliArchive.mapper.master.VideoBackupConfigMapper.selectById"))
    })
    List<BackupFav> selectAllEnabled();

    @Select("""
            SELECT * FROM backups_fav
            WHERE fav_id = #{favId}
            """)
    BackupFav selectByFavId(Long favId);

    @Select("select COUNT(1) from backups_fav WHERE fav_id = #{favId}")
    boolean checkExists(long favId);

    @Select("select * from backups_fav where video_backup_config_id = #{id}")
    List<BackupFav> selectByVideoBackupConfigId(int id);

}
