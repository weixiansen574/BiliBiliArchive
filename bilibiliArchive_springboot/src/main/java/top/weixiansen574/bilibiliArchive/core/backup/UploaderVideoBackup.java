package top.weixiansen574.bilibiliArchive.core.backup;

import top.weixiansen574.bilibiliArchive.bean.backups.BackupUploader;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.UploaderVideoInfo;
import top.weixiansen574.bilibiliArchive.core.ContentUpdateThread;
import top.weixiansen574.bilibiliArchive.core.PriorityManger;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.UploaderVideoPage;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.task.VideoBackupCall;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoUploaderMapper;

import java.io.IOException;
import java.util.*;

public class UploaderVideoBackup extends VideoBackup {

    VideoUploaderMapper videoUploaderMapper;
    List<UploaderVideoPage.UpVideo> upVideos;
    BackupUploader backupUploader;
    long upUid;

    public UploaderVideoBackup(UserContext userContext, PriorityManger priorityManger, ContentUpdateThread updateThread, BackupUploader backupUploader) {
        super(userContext, priorityManger, updateThread, backupUploader.videoBackupConfig);
        this.backupUploader = backupUploader;
        videoUploaderMapper = userContext.videoUploaderMapper;
        upUid = backupUploader.uid;
    }


    @Override
    public List<VideoBackupCall> queryPendingBackupVideos() throws Exception {
        UploaderVideoInfo latest = videoUploaderMapper.selectLatest(upUid);
        if (latest == null){
            //如果已备份的UP主视频列表是空的，则从设定的起始时间戳开始备份
            PG.content(getDesc()+"，第一次执行备份，下载设定时间之后发布的视频");
            upVideos = getUploaderVideosAfterACertainTime(backupUploader.backupStartTime != null ? backupUploader.backupStartTime : 0);
        } else {
            upVideos = getUploaderVideosAfterACertainTime(latest.created);
        }
        Collections.reverse(upVideos);//反转，使其发布时间更早的在前
        List<VideoBackupCall> videoBackupCallList = new LinkedList<>();
        for (UploaderVideoPage.UpVideo upVideo : upVideos) {
            VideoInfo videoInfo = biliApiService.getVideoInfoByBvid(upVideo.bvid).success();
            videoBackupCallList.add(new VideoBackupCall(videoInfo,userContext,priorityManger,updateThread,videoBackupConfig));
        }
        return videoBackupCallList;
    }

    @Override
    public void commit(Map<String, Boolean> downloadedBvidMap) {
        for (UploaderVideoPage.UpVideo upVideo : upVideos) {
            if (!downloadedBvidMap.get(upVideo.bvid)){
                return;
            }
            videoUploaderMapper.insert(upUid/*注意不要使用视频信息中的mid，因为合作视频有可能视频UP不是此UP*/,upVideo.bvid,upVideo.aid,upVideo.created);
        }
    }

    private List<UploaderVideoPage.UpVideo> getUploaderVideosAfterACertainTime(long secTimestamp) throws BiliBiliApiException, IOException {
        String referer = "https://space.bilibili.com/"+upUid+"/video";
        List<UploaderVideoPage.UpVideo> upVideos = new ArrayList<>();
        for (int pn = 1; true; pn++) {
            UploaderVideoPage page = biliApiService.getUploaderVideoPage(referer, upUid, pn).success();
            List<UploaderVideoPage.UpVideo> videoList = page.list.vlist;
            if (videoList == null || videoList.isEmpty()){
                return upVideos;
            }
            for (UploaderVideoPage.UpVideo upVideo : videoList) {
                if (upVideo.created > secTimestamp){
                    upVideos.add(upVideo);
                } else {
                    return upVideos;
                }
            }
        }
    }

    @Override
    public String getDesc() {
        return String.format("UP主：%s(%s)",backupUploader.name,backupUploader.uid);
    }
}
