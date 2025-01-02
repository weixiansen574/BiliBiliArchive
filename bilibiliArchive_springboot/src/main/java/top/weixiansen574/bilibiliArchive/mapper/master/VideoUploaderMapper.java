package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.UploaderVideoInfo;

import java.util.List;

@Repository
public interface VideoUploaderMapper {
    //videos_uploader

    @Insert("INSERT INTO videos_uploader (uid,bvid,avid,created) VALUES(#{uid},#{bvid},#{avid},#{created})")
    void insert(@Param("uid") long uid,@Param("bvid") String bvid,@Param("avid") long avid,@Param("created") long created);

    @Select("""
            SELECT vu.uid, vu.created, vi.*
            FROM (SELECT * FROM videos_uploader WHERE uid = #{uid}) AS vu
            LEFT JOIN video_infos AS vi ON vu.bvid = vi.bvid
            ORDER BY vu.created DESC
            LIMIT #{offset}, #{size}
            """)
    List<UploaderVideoInfo> selectPageByUploaderId(@Param("uid") long uid,
                                                   @Param("offset") int offset,
                                                   @Param("size") int size);

    @Select("""
            SELECT vu.uid, vu.created, vi.*
            FROM (SELECT * FROM videos_uploader WHERE uid = #{uid}) AS vu
            LEFT JOIN video_infos AS vi ON vu.bvid = vi.bvid
            ORDER BY vu.created DESC
            LIMIT 1
            """)
    UploaderVideoInfo selectLatest(@Param("uid") long uid);

    @Select("""
            SELECT vu.uid, vu.created, vi.*
            FROM (SELECT * FROM videos_uploader WHERE uid = #{uid}) AS vu
            LEFT JOIN video_infos AS vi ON vu.bvid = vi.bvid
            ORDER BY vu.created DESC
            """)
    List<UploaderVideoInfo> selectAllByUid(long uid);

    @Delete("delete from videos_uploader where uid = #{uid} AND bvid = #{bvid}")
    void deleteByPrimaryKey(long uid, String bvid);

    @Select("select COUNT(*) from videos_uploader where uid = #{upUid}")
    int selectVideoCount(long upUid);
}
