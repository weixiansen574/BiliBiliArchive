package top.weixiansen574.bilibiliArchive.mapper.comment;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.ByteArrayTypeHandler;
import org.springframework.stereotype.Repository;
import top.weixiansen574.bilibiliArchive.bean.CmAvatar;

@Repository
public interface AvatarMapper {

    @Insert("INSERT INTO avatars (name, data) VALUES (#{name}, #{data})")
    void insert(String name,byte[] data);

    @Delete("DELETE FROM avatars WHERE name = #{name}")
    void delete(@Param("name") String fileName);

    @Select("SELECT COUNT(1) FROM avatars WHERE name = #{name}")
    boolean checkExists(@Param("name") String name);


    @Select("SELECT * FROM avatars WHERE name = #{name}")
    @Results({
            @Result(column = "data", property = "data", typeHandler = ByteArrayTypeHandler.class)//特别适配SQLite
    })
    CmAvatar selectByName(@Param("name") String fileName);

}
