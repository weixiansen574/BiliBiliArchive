package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.BiliUser;

import java.util.List;

@Repository
public interface UserMapper {

    @Select("select * from users")
    List<BiliUser> selectAll();

    @Select("select * from users where uid = #{uid}")
    BiliUser selectByUid(long uid);

    @Insert("INSERT INTO users (uid,name,avatar_url,cookie,pfs) VALUES(#{uid},#{name},#{avatarUrl},#{cookie},#{pfs})")
    void insert(BiliUser user);

    @Delete("DELETE FROM users WHERE uid = #{uid}")
    void deleteByUid(long uid);

    @Update("UPDATE users SET name = #{name},avatar_url = #{avatarUrl},cookie = #{cookie},pfs = #{pfs} where uid = #{uid}")
    void update(BiliUser user);

    @Select("select COUNT(1) from users where uid = #{uid}")
    boolean checkExists(long uid);

    @Select("""
        SELECT
            CASE
                WHEN EXISTS (SELECT 1 FROM backups_fav WHERE backup_user_id = #{uid}) OR
                     EXISTS (SELECT 1 FROM backups_history WHERE uid = #{uid}) OR
                     EXISTS (SELECT 1 FROM backups_uploader WHERE backup_user_id = #{uid})
                THEN 1
                ELSE 0
            END AS is_referenced;
        """)
    boolean checkUserHasReferences(long uid);


}
