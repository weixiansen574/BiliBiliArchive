package top.weixiansen574.bilibiliArchive.core.task;

import top.weixiansen574.bilibiliArchive.bean.backups.BackupFav;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupUploader;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.FavoriteVideoInfo;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.UploaderVideoInfo;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.operation.progress.ProgressBar;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoFavoriteMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoInfoMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoUploaderMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Exchanger;

public class RefreshUpVideoStatesThread extends Thread{
    private final Exchanger<Integer> idChanger;
    private final BackupUploader backupUploader;
    private final long upUid;
    private final UserContext userContext;

    private final BiliApiService biliApiService;
    private final VideoUploaderMapper videoUploaderMapper;
    private final VideoInfoMapper videoInfoMapper;

    public RefreshUpVideoStatesThread(Exchanger<Integer> idChanger, UserContext userContext, BackupUploader backupUploader) {
        this.idChanger = idChanger;
        this.backupUploader = backupUploader;
        this.upUid = backupUploader.uid;
        this.userContext = userContext;

        this.biliApiService = userContext.biliApiService;
        this.videoUploaderMapper = userContext.videoUploaderMapper;
        this.videoInfoMapper = userContext.videoInfoMapper;
    }

    @Override
    public void run() {
        try {
            idChanger.exchange(PG.titleInit(String.format("更新UP主视频状态：「%s」",
                    backupUploader.name)));
        } catch (InterruptedException e) {
            PG.remove();
            return;
        }
        PG.content("正在查询视频列表");
        List<UploaderVideoInfo> uploaderVideoInfos = videoUploaderMapper.selectAllByUid(upUid);
        for (int i = 0; i < uploaderVideoInfos.size(); i++) {
            UploaderVideoInfo videoInfo = uploaderVideoInfos.get(i);
            String oldstate = videoInfo.state;
            try {
                //获取视频新状态
                MiscUtils.getVideoInfoOrChangeState(userContext,videoInfo);
                PG.contentAndData("%s『%s』状态：%s  ==> %s", new ProgressBar(uploaderVideoInfos.size(), i + 1),
                        videoInfo.bvid, MiscUtils.omit(videoInfo.title, 30), oldstate, videoInfo.state);
                videoInfoMapper.updateVideoStatus(videoInfo.state, videoInfo.bvid);
                MiscUtils.sleepNoException(100);
            } catch (IOException e) {
                PG.content("获取视频状态失败，发生IO异常：%s", e.getMessage());
                MiscUtils.sleepNoException(5000);
            } catch (BiliBiliApiException e) {
                PG.content(e.getMessage());
                MiscUtils.sleepNoException(10000);
            }
        }
        PG.remove();
    }
}
