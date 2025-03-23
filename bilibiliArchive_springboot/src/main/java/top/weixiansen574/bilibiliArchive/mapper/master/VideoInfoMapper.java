package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;

import java.util.List;

@Repository
public interface VideoInfoMapper {
    @Insert("""
            INSERT INTO video_infos (
                bvid, avid, title, `desc`,duration,owner_mid, owner_name, owner_avatar_url, staff,
                `view`, danmaku, favorite, coin, `like`, share, reply, tname, ctime, pubdate,
                cover_url, tags, pages_version_list, state, downloading, save_time, community_update_time,
                config_id,total_comment_floor,metadata_changes
            ) VALUES (
                #{bvid}, #{avid}, #{title},#{desc},#{duration},#{ownerMid}, #{ownerName},
                #{ownerAvatarUrl}, #{staff}, #{view}, #{danmaku}, #{favorite}, #{coin},
                #{like}, #{share}, #{reply}, #{tname}, #{ctime},#{pubdate}, #{coverUrl}, #{tags}, #{pagesVersionList},
                #{state}, #{downloading}, #{saveTime}, #{communityUpdateTime},
                #{configId},#{totalCommentFloor},#{metadataChanges}
            )
            """)
    void insert(ArchiveVideoInfo videoInfo);

    @Select("""
            SELECT
                CASE
                    WHEN EXISTS (SELECT 1 FROM videos_favorite WHERE bvid = #{bvid}) OR
                         EXISTS (SELECT 1 FROM videos_history WHERE bvid = #{bvid}) OR
                         EXISTS (SELECT 1 FROM videos_uploader WHERE bvid = #{bvid})
                    THEN 1
                    ELSE 0
                END AS is_referenced;
            """)
    boolean checkVideoIsReferenced(String bvid);

    @Select("SELECT * FROM video_infos WHERE bvid = #{bvid}")
    ArchiveVideoInfo selectByBvid(String bvid);

    @Update("UPDATE video_infos SET state = #{state} WHERE bvid = #{bvid}")
    void updateStatus(@Param("state") String status, @Param("bvid") String bvid);

    @Update("UPDATE video_infos SET downloading = #{downloading} WHERE bvid = #{bvid}")
    void updateDownloading(String bvid, int downloading);

    @Update("""
            UPDATE video_infos SET
            `view` = #{view},danmaku = #{danmaku},favorite = #{favorite},coin = #{coin},
            `like` = #{like},share = #{share},reply = #{reply},tags = #{tags},pages_version_list = #{pagesVersionList},
            downloading = #{downloading},community_update_time = #{communityUpdateTime},
            config_id = #{configId},duration = #{duration},staff = #{staff},
            state = #{state},total_comment_floor = #{totalCommentFloor},metadata_changes = #{metadataChanges},
            try_search = #{trySearch}
            WHERE bvid = #{bvid}
            """)
    void update(ArchiveVideoInfo videoInfo);

    @Update("UPDATE video_infos SET title = #{title},tname = #{tname},cover_url = #{coverUrl} WHERE bvid = #{bvid}")
    void supplementaryFailedVideoInfo(@Param("bvid") String bvid, @Param("title") String title, @Param("tname") String tagName,
                                      @Param("coverUrl") String coverUrl);

    @Update("UPDATE video_infos SET state = #{state} WHERE bvid = #{bvid}")
    void updateVideoStatus(@Param("state") String status, @Param("bvid") String bvid);

    @Delete("delete from video_infos where bvid = #{bvid}")
    void deleteByBvid(String bvid);

    @Select("SELECT COUNT(*) FROM video_infos where owner_avatar_url = #{url}")
    int selectUpAvatarCount(@Param("url") String avatarUrl);

    @Update("update video_infos set config_id = #{configId} where bvid = #{bvid}")
    void updateConfigId(String bvid, int configId);

    @Select("select * from video_infos where title LIKE #{searchText} ORDER BY save_time DESC")
    List<ArchiveVideoInfo> searchVideos(String searchText);

    @Select("""
            <script>
                SELECT * FROM video_infos
                <where>
                    <if test="searchText != null">title LIKE #{searchText}</if>
                </where>
                ORDER BY ${sortBy} ${sortType}
                LIMIT #{offset}, #{size}
            </script>
            """)
    List<ArchiveVideoInfo> selectBySearch(String searchText, String sortBy, String sortType, int offset, int size);
}
