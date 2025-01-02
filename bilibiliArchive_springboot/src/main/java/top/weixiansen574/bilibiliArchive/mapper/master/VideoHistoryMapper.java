package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.HistoryVideoInfo;

import java.util.List;

@Repository
public interface VideoHistoryMapper {


    @Insert("INSERT INTO videos_history (uid, bvid, avid, view_at) VALUES (#{uid}, #{bvid}, #{avid}, #{viewAt})")
    void insert(@Param("uid") long uid, @Param("bvid") String bvid, @Param("avid") long avid, @Param("viewAt") long viewAt);

    @Select("""
            <script>
            SELECT uhv.uid,
                     uhv.view_at,
                     vi.*
            FROM (SELECT * FROM videos_history
                WHERE uid = #{uid}
                <when test='asOf != null'>AND view_at &lt; #{asOf}</when>
                ) AS uhv
            LEFT JOIN video_infos AS vi
                ON uhv.bvid = vi.bvid
            <when test='failedOnly'>WHERE state != "NORMAL"</when>
            ORDER BY view_at DESC LIMIT #{limit} OFFSET #{offset};
            </script>
            """)
    List<HistoryVideoInfo> selectPageByUid(@Param("uid") long uid, @Param("limit") int limit, @Param("offset") int offset,
                                           @Param("failedOnly") boolean failedOnly, @Param("asOf") Long asOf);

    @Select("""
            SELECT vh.*, vi.*
            FROM videos_history vh
            JOIN video_infos vi ON vh.bvid = vi.bvid
            WHERE uid = #{uid} AND vh.view_at < #{time} AND vi.state = 'NORMAL'
            ORDER BY vh.view_at DESC
            """)
    List<HistoryVideoInfo> selectNoFaiVideosBeforeTime(@Param("uid") long uid,@Param("time") long time);

    @Select("SELECT * FROM videos_history WHERE uid = #{uid} ORDER BY view_at DESC LIMIT 1")
    HistoryVideoInfo selectLatest(@Param("uid") long uid);

    @Update("UPDATE videos_history SET view_at = #{viewAt} WHERE uid = #{uid} AND bvid = #{bvid}")
    void updateViewAt(@Param("viewAt") long viewAt, @Param("uid") long uid, @Param("bvid") String bvid);

    @Select("SELECT COUNT(1) FROM videos_history WHERE uid = #{uid} AND bvid = #{bvid}")
    boolean checkExists(@Param("uid") long uid, @Param("bvid") String bvid);

    @Select("""
            SELECT vh.*, vi.*
            FROM videos_history vh
            JOIN video_infos vi ON vh.bvid = vi.bvid
            WHERE uid = #{uid} AND vi.state = 'NORMAL'
            ORDER BY vh.view_at DESC
            LIMIT -1 OFFSET #{offset}
            """)
    List<HistoryVideoInfo> selectNoFaiVideosFromOffset(@Param("uid") long uid,@Param("offset") int offset);

    @Select("""
            SELECT vh.*, vi.*
            FROM videos_history vh
            JOIN video_infos vi ON vh.bvid = vi.bvid
            WHERE uid = #{uid} AND vi.state = 'NORMAL'
            """)
    Cursor<HistoryVideoInfo> selectAllNoFaiByUid(long uid);

    @Select("""
            SELECT vh.*, vi.*
            FROM videos_history vh
            JOIN video_infos vi ON vh.bvid = vi.bvid
            WHERE uid = #{uid}
            """)
    Cursor<HistoryVideoInfo> selectAllByUid(long uid);

    @Delete("delete from videos_history where uid = #{uid} AND bvid = #{bvid}")
    void deleteByPrimaryKey(long uid, String bvid);

    @Select("select COUNT(*) from videos_history where uid = #{uid}")
    int selectVideoCount(long uid);
}
