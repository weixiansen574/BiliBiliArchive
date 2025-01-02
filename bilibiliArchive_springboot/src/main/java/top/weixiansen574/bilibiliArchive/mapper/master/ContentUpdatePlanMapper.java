package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.contentupdate.VideoUpdatePlan;

import java.util.List;

@Repository
public interface ContentUpdatePlanMapper {
    @Insert("INSERT INTO video_update_plans (timestamp, uid, bvid, avid, title, update_video_and_danmaku, video_quality, video_codec_id, comment_config_1, comment_config_2, comment_config_3) " +
            "VALUES (#{timestamp}, #{uid}, #{bvid}, #{avid}, #{title}, #{updateVideoAndDanmaku}, #{videoQuality}, #{videoCodecId}, #{commentConfig1}, #{commentConfig2}, #{commentConfig3})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertVideoUpdatePlan(VideoUpdatePlan videoUpdatePlan);

    @Select("SELECT * FROM video_update_plans ORDER BY timestamp ASC LIMIT 1")
    VideoUpdatePlan selectEarliestVideoUpdatePlan();

    @Select("""
            <script>
            SELECT * FROM video_update_plans
            <where>
              <if test='bvid != null'>
                bvid = #{bvid}
              </if>
            </where>
            ORDER BY timestamp ASC
            LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<VideoUpdatePlan> selectVideoPlansAscPage(@Param("offset") int offset, @Param("size") int size, @Param("bvid") String bvid);


    @Delete("DELETE FROM video_update_plans WHERE bvid = #{bvid}")
    int deleteVideoUpdatePlanByBvid(@Param("bvid") String bvid);

    @Delete("DELETE FROM video_update_plans WHERE id = #{id}")
    int deleteVideoUpdatePlanById(@Param("id") long id);

    @Select("select COUNT(1) from video_update_plans where bvid = #{bvid}")
    boolean checkVideoHasUpdatePlan(String bvid);
}
