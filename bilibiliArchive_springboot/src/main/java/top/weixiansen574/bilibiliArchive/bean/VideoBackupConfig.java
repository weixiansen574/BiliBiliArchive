package top.weixiansen574.bilibiliArchive.bean;

import com.alibaba.fastjson2.JSON;
import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.config.VideoContentUpdateConfig;
import top.weixiansen574.bilibiliArchive.bean.config.VideoDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.list.VideoContentUpdateConfigList;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

import java.util.List;

public class VideoBackupConfig {
    public Integer id;
    public String name;
    public VideoDownloadConfig video;
    public CommentDownloadConfig comment;//为null不备份评论
    public VideoContentUpdateConfigList update;//不能为null，但可以空，表示不更新

    @Override
    public String toString() {
        return "VideoBackupConfig{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", video=" + video +
                ", comment=" + comment +
                ", update=" + update +
                '}';
    }

    public boolean check(){
        if (!MiscUtils.notNulls(id,name,video,update)) {
            return false;
        }
        if (!video.check()){
            return false;
        }
        if (comment != null){
            if (!comment.check()) {
                return false;
            }
        }
        for (VideoContentUpdateConfig videoContentUpdateConfig : update) {
            if (!videoContentUpdateConfig.check()){
                return false;
            }
        }
        return true;
    }

}
