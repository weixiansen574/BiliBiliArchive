package top.weixiansen574.bilibiliArchive.mapper.comment;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.ArchiveComment;

import java.util.List;

@Repository
public interface CommentMapper {
    int AREA_TYPE_VIDEO = 1;

    @Insert("""
            INSERT INTO comments (
                rpid, oid, type, mid, root, parent, uname,
                current_level, location, message, `like`, ctime,
                pictures, avatar_url,vip_type,floor
            ) VALUES (
                #{rpid}, #{oid}, #{type}, #{mid}, #{root}, #{parent}, #{uname},
                #{currentLevel}, #{location}, #{message}, #{like}, #{ctime},
                #{pictures}, #{avatarUrl}, #{vipType}, #{floor}
            )
            """)
    void insert(ArchiveComment comment);

    @Update("UPDATE comments SET `like` = #{like} WHERE rpid = #{rpid}")
    void updateLike(@Param("like") int like, @Param("rpid") long rpid);

    @Update("UPDATE comments SET `like` = #{like},floor = #{floor} WHERE rpid = #{rpid}")
    void updateLikeAndFloor(@Param("like") int like,@Param("floor") int floor, @Param("rpid") long rpid);

    @Select("SELECT COUNT(1) FROM comments WHERE rpid = #{rpid}")
    boolean checkExists(long rpid);

    @Select("SELECT * FROM comments WHERE oid = #{oid} AND type = #{type} AND root = 0 ORDER BY ctime DESC LIMIT 1")
    ArchiveComment selectLatestPostedRootComment(@Param("oid") long oid, @Param("type") int type);

    @Select("select * from comments WHERE oid = #{oid} AND type = #{type} AND root = #{root} ORDER BY ctime DESC LIMIT 1")
    ArchiveComment selectLatestPostedReplyComment(@Param("oid") long oid, @Param("type") int type,@Param("root") long root);

    @Select("select * from comments where oid = #{oid} AND type = #{type} ORDER BY floor DESC LIMIT 1;")
    ArchiveComment selectTopFloorRootComment(@Param("oid") long oid, @Param("type") int type);

    @Select("SELECT COUNT(*) FROM comments WHERE oid = #{oid} AND type = #{type}")
    int selectCommentAllCountForOid(@Param("oid") long oid, @Param("type") int type);

    @Select("SELECT COUNT(*) FROM comments WHERE oid = #{oid} AND type = #{type} AND root = 0 ")
    int selectRootCommentCountForOid(@Param("oid") long oid, @Param("type") int type);

    @Select("""
        SELECT * FROM comments
        WHERE oid = #{oid} AND type = #{type} AND root = 0
        ORDER BY `${orderByField}` DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<ArchiveComment> rangeQueryRootComments(
            @Param("oid") long oid,
            @Param("type") int type,
            @Param("orderByField") String orderByField,
            @Param("limit") int limit,
            @Param("offset") int offset
    );


    @Select("SELECT COUNT(*) from comments WHERE root = #{root}")
    int selectCommentReplyCount(@Param("root") long rootRpid);

    @Select("SELECT * FROM comments WHERE root = #{root} ORDER BY ctime ASC LIMIT #{limit} OFFSET #{offset}")
    List<ArchiveComment> selectRepliesWithPagination(@Param("root") long rootRpid, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT * FROM comments WHERE root = #{root} ORDER BY ctime ASC")
    List<ArchiveComment> selectAllReplies(@Param("root") long rootRpid);

    @Select("SELECT * FROM comments WHERE rpid = #{rpid}")
    ArchiveComment selectByRpid(@Param("rpid") long rpid);

    @Select("SELECT * FROM comments WHERE oid = #{oid} AND type = #{type}")
    Cursor<ArchiveComment> selectAllByArea(long oid,int type);

    @Select("SELECT COUNT(*) FROM comments WHERE avatar_url = #{avatarUrl}")
    int selectAvatarCount(@Param("avatarUrl") String avatarUrl);

    @Delete("delete from comments where rpid = #{rpid}")
    void deleteByRpid(long rpid);

    @Delete("DELETE FROM comments WHERE oid = #{oid} AND type = #{type}")
    int deleteByArea(long oid,int type);

    //仅供特殊用途
    @Select("select * from comments")
    Cursor<ArchiveComment> selectAll();
}
