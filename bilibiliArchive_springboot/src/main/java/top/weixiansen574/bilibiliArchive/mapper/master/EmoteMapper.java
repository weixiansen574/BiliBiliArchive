package top.weixiansen574.bilibiliArchive.mapper.master;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.Emote;

import java.util.List;

@Repository
public interface EmoteMapper {
    // 插入记录
    @Insert("INSERT INTO emote (id, text, size, url, file_name) " +
            "VALUES (#{id}, #{text}, #{size}, #{url}, #{fileName})")
    int insertEmote(Emote emote);

    // 根据id删除记录
    @Delete("DELETE FROM emote WHERE id = #{id}")
    boolean deleteEmoteById(long id);

    // 更新记录
    @Update("UPDATE emote SET text = #{text}, size = #{size}, url = #{url}, file_name = #{fileName} " +
            "WHERE id = #{id}")
    int updateEmote(Emote emote);

    // 根据id查询记录
    @Select("SELECT id, text, size, url, file_name FROM emote WHERE id = #{id}")
    Emote selectEmoteById(long id);

    // 查询所有记录
    @Select("SELECT id, text, size, url, file_name FROM emote")
    List<Emote> selectAllEmotes();
}
