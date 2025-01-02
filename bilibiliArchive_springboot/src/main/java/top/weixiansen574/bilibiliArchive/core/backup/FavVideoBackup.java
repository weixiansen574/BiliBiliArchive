package top.weixiansen574.bilibiliArchive.core.backup;

import top.weixiansen574.bilibiliArchive.bean.backups.BackupFav;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.FavoriteVideoInfo;
import top.weixiansen574.bilibiliArchive.core.ContentUpdateThread;
import top.weixiansen574.bilibiliArchive.core.PriorityManger;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.task.VideoBackupCall;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.FavoriteVideoItem;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.DetailedFavoriteVideoInfosPage;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoFavoriteMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FavVideoBackup extends VideoBackup {
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_FAILED = 1;
    public static final int STATUS_UP_DELETED = 9;

    private List<PendingDownFavoriteVideo> pendingVideos;

    private final BackupFav fav;
    private final VideoFavoriteMapper videoFavoriteMapper;

    public FavVideoBackup(UserContext userContext, PriorityManger priorityManager, ContentUpdateThread updateThread, BackupFav fav) {
        super(userContext, priorityManager, updateThread, fav.videoBackupConfig);
        this.fav = fav;
        this.videoFavoriteMapper = userContext.videoFavoriteMapper;
    }

    @Override
    public List<VideoBackupCall> queryPendingBackupVideos() throws Exception {
        FavoriteVideoInfo latestVideo = videoFavoriteMapper.selectLatest(fav.favId);
        long anchorTime = (latestVideo != null) ? latestVideo.favTime : 0;

        pendingVideos = fetchPendingVideosSinceFavTime(anchorTime);
        //翻转列表，使下载顺序从晚到早
        Collections.reverse(pendingVideos);
        return buildVideoBackupCalls();
    }

    /**
     * 自上次指定时间以来获取添加到收藏夹的视频。
     *
     * @param favTime 锚点视频的时间戳。
     * @return 待备份视频的列表。
     */
    private List<PendingDownFavoriteVideo> fetchPendingVideosSinceFavTime(long favTime) throws BiliBiliApiException, IOException {
        List<PendingDownFavoriteVideo> result = new LinkedList<>();
        for (int page = 1; ; page++) {
            DetailedFavoriteVideoInfosPage videoPage = biliApiService
                    .getFavoriteVideosInDetailed(fav.favId, page, 20, 0)
                    .success();

            if (videoPage.medias == null || videoPage.medias.isEmpty()) {
                return result;
            }

            for (FavoriteVideoItem videoItem : videoPage.medias) {
                if (videoItem.fav_time <= favTime) {
                    return result;
                }
                result.add(new PendingDownFavoriteVideo(
                        videoItem,
                        biliApiService.getVideoInfoByBvid(videoItem.bvid).success(),
                        videoFavoriteMapper.checkExists(fav.favId, videoItem.bvid)));
            }
        }
    }

    /**
     * 从待备份视频中构建备份任务列表。
     *
     * @return VideoBackupCall 的列表。
     */
    private List<VideoBackupCall> buildVideoBackupCalls() {
        List<VideoBackupCall> backupCalls = new LinkedList<>();
        for (PendingDownFavoriteVideo video : pendingVideos) {
            if (video.favVideoInfo.attr == STATUS_NORMAL &&
                    !videoFavoriteMapper.checkExists(fav.favId, video.videoInfo.bvid)) {
                backupCalls.add(new VideoBackupCall(
                        video.videoInfo, userContext, priorityManger, updateThread, videoBackupConfig));
            }
        }
        return backupCalls;
    }

    @Override
    public void commit(Map<String, Boolean> downloadedVideos) {
        for (PendingDownFavoriteVideo video : pendingVideos) {
            FavoriteVideoItem videoInfo = video.favVideoInfo;

            if (videoFavoriteMapper.checkExists(fav.favId, videoInfo.bvid)) {
                videoFavoriteMapper.updateFavTime(videoInfo.fav_time, fav.favId, videoInfo.bvid);
                continue;
            }

            switch (videoInfo.attr) {
                case STATUS_NORMAL -> handleNormalVideo(videoInfo, downloadedVideos);
                case STATUS_FAILED -> handleFailedVideo(videoInfo, "已失效！备份失败！");
                case STATUS_UP_DELETED -> handleFailedVideo(videoInfo, "已被UP主删除！备份失败！");
            }
        }
    }

    /**
     * 处理正常状态的视频。
     *
     * @param videoInfo        当前视频的信息。
     * @param downloadedVideos 已下载视频的映射。
     */
    private void handleNormalVideo(FavoriteVideoItem videoInfo, Map<String, Boolean> downloadedVideos) {
        if (!downloadedVideos.getOrDefault(videoInfo.bvid, false)) {
            return;
        }
        videoFavoriteMapper.insert(fav.favId, videoInfo.bvid, videoInfo.id, videoInfo.fav_time, false);
    }

    /**
     * 处理失败状态的视频。
     *
     * @param videoInfo 当前视频的信息。
     * @param message   提示消息。
     */
    private void handleFailedVideo(FavoriteVideoItem videoInfo, String message) {
        insertFailedVideo(videoInfo);
        PG.contentAndPrintf("收藏夹中BV号为：%s的视频%s请尽早在视频失效前启动备份姬≡(▔﹏▔)≡", videoInfo.bvid, message);
    }

    /**
     * 插入失败的视频记录。
     *
     * @param videoInfo 当前视频的信息。
     */
    private void insertFailedVideo(FavoriteVideoItem videoInfo) {
        ArchiveVideoInfo archiveInfo = new ArchiveVideoInfo();
        archiveInfo.bvid = videoInfo.bvid;
        archiveInfo.avid = videoInfo.id;
        archiveInfo.ownerName = videoInfo.upper.name;
        archiveInfo.ownerMid = videoInfo.upper.mid;
        archiveInfo.favorite = videoInfo.cnt_info.collect;
        archiveInfo.view = videoInfo.cnt_info.play;
        archiveInfo.danmaku = videoInfo.cnt_info.danmaku;
        archiveInfo.pagesVersionList = null;
        archiveInfo.state = ArchiveVideoInfo.STATE_FAILED_AND_NO_BACKUP;
        archiveInfo.title = "[在备份前已失效]";
        archiveInfo.downloading = 0;
        archiveInfo.configId = 0;
        archiveInfo.saveTime = System.currentTimeMillis();
        archiveInfo.communityUpdateTime = System.currentTimeMillis();

        videoInfoMapper.insert(archiveInfo);
        videoFavoriteMapper.insert(fav.favId, videoInfo.bvid, videoInfo.id, videoInfo.fav_time, false);
    }

    @Override
    public String getDesc() {
        return fav.ownerName + " 的收藏夹：" + fav.favName;
    }

    private record PendingDownFavoriteVideo(FavoriteVideoItem favVideoInfo, VideoInfo videoInfo, boolean existsInDb) {
    }
}

//觉得自己代码太难看，以上用ChatGPT重写了一遍
/*
public class FavVideoBackup extends VideoBackup{
    public static final int ATTR_NORMAL = 0;
    public static final int ATTR_FAILED = 1;
    public static final int ATTR_UP_DELETED = 9;

    private List<PendingDownFavoriteVideo> pendingDownVideos;

    private final BackupFav fav;
    private final VideoFavoriteMapper videoFavoriteMapper;

    public FavVideoBackup(UserContext userContext, PriorityManger priorityManger, ContentUpdateThread contentUpdateThread,
                          BackupFav fav) {
        super(userContext, priorityManger, contentUpdateThread, fav.videoBackupConfig);
        this.fav = fav;
        this.videoFavoriteMapper = userContext.videoFavoriteMapper;
    }

    @Override
    public List<VideoBackupCall> queryPendingBackupVideos() throws Exception{
        FavoriteVideoInfo latest = videoFavoriteMapper.selectLatest(fav.favId);
        if (latest != null) {
            pendingDownVideos = getNewPendingDownVideosByAnchorPoint(latest.favTime);
        } else {
            pendingDownVideos = getNewPendingDownVideosByAnchorPoint(0);
        }
        //翻转列表，使下载顺序从晚到早
        Collections.reverse(pendingDownVideos);

        List<VideoBackupCall> videoBackupCallList = new LinkedList<>();

        for (PendingDownFavoriteVideo pendingDownVideo : pendingDownVideos) {
            if (pendingDownVideo.favVideoInfo.attr == ATTR_NORMAL){
                if (!videoFavoriteMapper.checkExists(fav.favId, pendingDownVideo.videoInfo.bvid)){
                    videoBackupCallList.add(new VideoBackupCall(pendingDownVideo.videoInfo,
                            userContext,priorityManger, updateThread,videoBackupConfig));
                }
            }
        }

        return videoBackupCallList;
    }

    */
/**
 * 翻收藏夹列表直到找到定位点
 *
 * @param favTime 定位点视频的收藏时间
 * @return 定位点前面的视频列表（也就是未下载的），按收藏时间从新到旧
 *//*

    private List<PendingDownFavoriteVideo> getNewPendingDownVideosByAnchorPoint(long favTime) throws BiliBiliApiException, IOException {
        List<PendingDownFavoriteVideo> pendingDownVideos = new LinkedList<>();
        for (int pn = 1; true; pn++) {
            DetailedFavoriteVideoInfosPage page = biliApiService
                    .getFavoriteVideosInDetailed(fav.favId, pn, 20, 0)
                    .success();
            if (page.medias == null || page.medias.size() == 0) {
                return pendingDownVideos;
            }
            for (FavoriteVideoItem info : page.medias) {
                if (info.fav_time <= favTime) {
                    return pendingDownVideos;
                }
                pendingDownVideos.add(new PendingDownFavoriteVideo(info,
                        biliApiService.getVideoInfoByBvid(info.bvid).success(),
                        videoFavoriteMapper.checkExists(fav.favId, info.bvid)));
            }
        }
    }

    @Override
    public void commit(Map<String, Boolean> downloadedBvidMap) {
        for (PendingDownFavoriteVideo PDPendingDownFavoriteVideo : pendingDownVideos) {
            FavoriteVideoItem favVideoInfo = PDPendingDownFavoriteVideo.favVideoInfo;
            //你可能会删除收藏，然后再收藏回来，导致重复获取。遇到对应的，像历史记录那样改收藏时间
            if (videoFavoriteMapper.checkExists(fav.favId, favVideoInfo.bvid)) {
                videoFavoriteMapper.updateFavTime(favVideoInfo.fav_time, fav.favId,favVideoInfo.bvid);
                continue;
            }
            switch (favVideoInfo.attr) {
                case ATTR_NORMAL -> {
                    //若有视频下载失败，列表提交到此为止
                    if (!downloadedBvidMap.get(favVideoInfo.bvid)){
                        return;
                    }
                    String bvid = favVideoInfo.bvid;
                    videoFavoriteMapper.insert(fav.favId, bvid,favVideoInfo.id,favVideoInfo.fav_time,false);
                }
                case ATTR_FAILED -> {
                    insertFailedFavVideo(favVideoInfo);
                    PG.contentAndPrintf("收藏夹中BV号为：%s的视频已失效！备份失败！请尽早在视频失效前启动备份姬≡(▔﹏▔)≡", favVideoInfo.bvid);
                }
                case ATTR_UP_DELETED -> {
                    insertFailedFavVideo(favVideoInfo);
                    PG.contentAndPrintf("收藏夹中BV号为：%s的视频已被UP主删除！备份失败！请尽早在视频失效前启动备份姬≡(▔﹏▔)≡", favVideoInfo.bvid);
                }
            }
        }
    }
    private void insertFailedFavVideo(FavoriteVideoItem videoInfo) {
        ArchiveVideoInfo info = new ArchiveVideoInfo();
        info.bvid = videoInfo.bvid;
        info.avid = videoInfo.id;
        info.ownerName = videoInfo.upper.name;
        info.ownerMid = videoInfo.upper.mid;
        info.favorite = videoInfo.cnt_info.collect;
        info.view = videoInfo.cnt_info.play;
        info.danmaku = videoInfo.cnt_info.danmaku;
        info.pagesVersionList = null;
        info.state = ArchiveVideoInfo.STATE_FAILED_AND_NO_BACKUP;
        info.title = "[在备份前已失效]";
        info.downloading = 0;
        info.configId = 0;
        info.saveTime = System.currentTimeMillis();
        info.communityUpdateTime = System.currentTimeMillis();
        videoInfoMapper.insert(info);
        videoFavoriteMapper.insert(fav.favId, videoInfo.bvid,
                videoInfo.id, videoInfo.fav_time,false);
    }

    @Override
    public String getDesc() {
        return fav.ownerName+" 的收藏夹："+fav.favName;
    }

    private record PendingDownFavoriteVideo(FavoriteVideoItem favVideoInfo, VideoInfo videoInfo, boolean isUpdateFavTime){}

}
*/
