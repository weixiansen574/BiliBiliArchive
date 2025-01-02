package top.weixiansen574.bilibiliArchive.bean;

import top.weixiansen574.bilibiliArchive.bean.config.VideoContentUpdateConfig;
import top.weixiansen574.bilibiliArchive.bean.config.VideoDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.list.VideoContentUpdateConfigList;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

public class AddVideoUpdatePlansRequest {
    public String bvid;
    public Long uid;
    public VideoDownloadConfig video;
    public VideoContentUpdateConfigList update;

    public boolean check(){
        if (!MiscUtils.notNulls(bvid,uid, video)) {
            return false;
        }
        if (!video.check()){
            return false;
        }
        if (update == null){
            return false;
        }
        for (VideoContentUpdateConfig config : update) {
            if (!config.check()){
                return false;
            }
        }
        return true;
    }
}
