package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.FavoriteVideoInfo;

import java.util.List;

@Repository
public interface VideoFavoriteMapper {

    @Select("""
            SELECT uvf.fav_id, uvf.fav_time, uvf.fav_ban, vi.*
            FROM (SELECT * FROM videos_favorite WHERE fav_id = #{favId}) AS uvf
            LEFT JOIN video_infos AS vi ON uvf.bvid = vi.bvid ORDER BY fav_time DESC
            """)
    List<FavoriteVideoInfo> selectAllByFavId(@Param("favId") long favId);

    @Select("""
        SELECT uvf.fav_id, uvf.fav_time, uvf.fav_ban, vi.*
        FROM (SELECT * FROM videos_favorite WHERE fav_id = #{favId}) AS uvf
        LEFT JOIN video_infos AS vi ON uvf.bvid = vi.bvid
        ORDER BY uvf.fav_time DESC
        LIMIT #{offset}, #{size}
        """)
    List<FavoriteVideoInfo> selectPageByFavId(@Param("favId") long favId, @Param("offset") int offset, @Param("size") int size);

    @Insert("INSERT INTO videos_favorite (fav_id, bvid, avid, fav_time, fav_ban) VALUES " +
            "(#{favoritesId}, #{bvid}, #{avid}, #{favTime}, #{favBan})")
    void insert(@Param("favoritesId") long favoritesId, @Param("bvid") String bvid,
                @Param("avid") long avid, @Param("favTime") long favTime,
                @Param("favBan") boolean favBan);

    @Select("""
            SELECT uvf.fav_id, uvf.fav_time, uvf.fav_ban, vi.*
            FROM (SELECT * FROM videos_favorite WHERE fav_id = #{favId}) AS uvf
            LEFT JOIN video_infos AS vi ON uvf.bvid = vi.bvid ORDER BY fav_time DESC LIMIT 1
            """)
    FavoriteVideoInfo selectLatest(@Param("favId") long favId);

    @Select("SELECT COUNT(1) FROM videos_favorite WHERE fav_id = #{favId} AND bvid = #{bvid}")
    boolean checkExists(Long favId, String bvid);

    @Select("SELECT COUNT(*) FROM videos_favorite WHERE fav_id = #{favId}")
    int selectVideoCount(@Param("favId") long favId);

    @Delete("delete from videos_favorite where fav_id = #{favId} AND bvid = #{bvid}")
    void deleteByPrimaryKey(long favId, String bvid);

    @Update("update videos_favorite set fav_time = #{favTime} where fav_id = #{favId} AND bvid = #{bvid}")
    void updateFavTime(long favTime, long favId, String bvid);

    @Update("update videos_favorite set fav_ban = #{isFvaBan} where fav_id = #{favId} AND bvid = #{bvid}")
    void updateIsFavBan(boolean isFvaBan, long favId, String bvid);
}
