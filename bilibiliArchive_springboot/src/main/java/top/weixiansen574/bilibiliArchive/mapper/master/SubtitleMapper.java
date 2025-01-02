package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.Subtitle;

@Repository
public interface SubtitleMapper {
    @Insert("INSERT INTO subtitles(id,lan,lan_doc,content) VALUES (#{id},#{lan},#{lan_doc},#{content})")
    void insert(Subtitle subtitle);

    @Select("SELECT COUNT(1) FROM subtitles WHERE id = #{id}")
    boolean checkExists(@Param("id") long id);

    @Select("SELECT * FROM subtitles WHERE id = #{id}")
    Subtitle selectById(@Param("id") long id);

    @Delete("DELETE FROM subtitles WHERE id = #{id}")
    void delete(@Param("id") long id);
}
