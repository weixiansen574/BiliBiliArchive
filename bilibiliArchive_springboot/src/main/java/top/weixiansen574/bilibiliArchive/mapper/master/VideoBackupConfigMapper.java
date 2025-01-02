package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;

import java.util.List;

@Repository
public interface VideoBackupConfigMapper {
    int ID_FINAL = 0;

    @Select("select * from video_backup_configs where id = #{id}")
    VideoBackupConfig selectById(int id);

    @Select("select * from video_backup_configs where name = #{name}")
    VideoBackupConfig selectByName(String name);

    @Select("select * from video_backup_configs where id != 0")
    List<VideoBackupConfig> selectAll();

    @Insert("insert into video_backup_configs (id,name,video,comment,`update`) values(null,#{name},#{video},#{comment},#{update})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(VideoBackupConfig videoBackupConfig);

    @Update("update video_backup_configs set name = #{name},video = #{video},comment = #{comment},`update` = #{update} where id = #{id}")
    boolean update(VideoBackupConfig videoBackupConfig);

    @Select("select count(1) from video_backup_configs where id = #{id}")
    boolean checkExists(int id);

    @Select("select count(1) from video_backup_configs where name = #{name} AND id != #{id}")
    boolean checkNameExistsExceptId(String name,int id);

    @Select("select count(1) from video_backup_configs where name = #{name}")
    boolean checkExistsByName(String name);

    @Delete("delete from video_backup_configs where id = #{id}")
    void deleteById(int id);

    @Update("update video_infos set config_id = #{newId} where config_id = #{oldId}")
    void updateVideoInfoConfigIdTo(int newId, int oldId);
}
