package top.weixiansen574.bilibiliArchive.core.backup;

import top.weixiansen574.bilibiliArchive.bean.BiliUser;
import top.weixiansen574.bilibiliArchive.bean.BlackListUser;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupHistory;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.HistoryVideoInfo;
import top.weixiansen574.bilibiliArchive.core.ContentUpdateThread;
import top.weixiansen574.bilibiliArchive.core.PriorityManger;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.task.VideoBackupCall;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.GeneralResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.HistoriesPage;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoHistoryMapper;
import top.weixiansen574.bilibiliArchive.services.ArchiveDeleteService;

import java.io.IOException;
import java.util.*;

public class HistoryVideoBackup extends VideoBackup {
    private final VideoHistoryMapper videoHistoryMapper;
    private final BackupHistory his;
    private List<PendingDownloadHisVideo> pendingDownloadHisVideos;
    private final BiliUser user;
    private final Set<Long> blackListUidSet = new HashSet<>();
    private final ArchiveDeleteService archiveDeleteService;

    public HistoryVideoBackup(UserContext userContext, PriorityManger priorityManger, ContentUpdateThread contentUpdateThread,
                              BackupHistory his, List<BlackListUser> globalUpBlackList) {
        super(userContext, priorityManger, contentUpdateThread, his.videoBackupConfig);
        this.videoHistoryMapper = userContext.videoHistoryMapper;
        this.archiveDeleteService = userContext.archiveDeleteService;
        this.his = his;
        this.user = userContext.biliUser;
        if (globalUpBlackList != null) {
            for (BlackListUser blackListUser : globalUpBlackList) {
                blackListUidSet.add(blackListUser.uid);
            }
        }
        if (his.uploaderBlackList != null) {
            for (BlackListUser blackListUser : his.uploaderBlackList) {
                blackListUidSet.add(blackListUser.uid);
            }
        }
    }

    @Override
    public List<VideoBackupCall> queryPendingBackupVideos() throws Exception {
        List<VideoBackupCall> videoBackupCallList = new ArrayList<>();

        HistoryVideoInfo latestVideo = videoHistoryMapper.selectLatest(his.uid);
        if (latestVideo == null) {
            PG.content("第一次启动历史记录备份，下载列表第一个视频");
            HistoriesPage histories = biliApiService.getLatestVideoHistories(20).success("下载第一条历史记录失败");
            HistoriesPage.HistoryItem historyItem = histories.list.get(0);
            String bvid = historyItem.history.bvid;
            VideoInfo videoInfo = biliApiService.getVideoInfoByBvid(bvid).success("第一条历史记录视频状态非正常");
            pendingDownloadHisVideos = new ArrayList<>(1);
            pendingDownloadHisVideos.add(new PendingDownloadHisVideo(videoInfo, historyItem, false));
            videoBackupCallList.add(new VideoBackupCall(videoInfo, userContext, priorityManger, updateThread, videoBackupConfig));
            return videoBackupCallList;
        }
        pendingDownloadHisVideos = getPendingDownloadHisVideos(latestVideo.viewAt);
        Collections.reverse(pendingDownloadHisVideos);
        for (PendingDownloadHisVideo video : pendingDownloadHisVideos) {
            if (!video.isUpdateViewAt) {
                videoBackupCallList.add(new VideoBackupCall(video.videoInfo, userContext, priorityManger,
                        updateThread, videoBackupConfig));
            }
        }
        return videoBackupCallList;
    }

    @Override
    public void commit(Map<String, Boolean> downloadedBvidMap) throws Exception {
        for (PendingDownloadHisVideo video : pendingDownloadHisVideos) {
            VideoInfo videoInfo = video.videoInfo;
            long viewAt = video.historyItem.view_at;
            if (video.isUpdateViewAt) {
                videoHistoryMapper.updateViewAt(viewAt, his.uid, videoInfo.bvid);
            } else {
                //若有其中一个视频下载时出现问题，提交的历史记录将截止这个之前
                if (!downloadedBvidMap.get(video.videoInfo.bvid)) {
                    return;
                }
                videoHistoryMapper.insert(his.uid, videoInfo.bvid, videoInfo.aid, viewAt);
            }
        }
        if (his.autoDeleteMethod.equals(BackupHistory.DELETE_METHOD_DISABLE)) {
            return;
        }

        List<HistoryVideoInfo> pendingDeleteVideos = new LinkedList<>();
        switch (his.autoDeleteMethod){
            case BackupHistory.DELETE_METHOD_DAYS -> {
                PG.content("准备删除" + his.deleteByDays + "天前观看且未失效的视频");
                long timeStamp = System.currentTimeMillis() / 1000 - (his.deleteByDays * 24 * 60 * 60);
                pendingDeleteVideos.addAll(videoHistoryMapper.selectNoFaiVideosBeforeTime(his.uid, timeStamp));
            }
            case BackupHistory.DELETE_METHOD_ITEM_QUANTITY -> {
                PG.content("准备删除历史记录列表" + his.deleteByItemQuantity + "条之外的未失效视频");
                pendingDeleteVideos.addAll(videoHistoryMapper.selectNoFaiVideosFromOffset(his.uid,his.deleteByItemQuantity));
            }
            case BackupHistory.DELETE_METHOD_DISK_USAGE -> {
                PG.content("准备删除硬盘占用超过 %s%s 往后且未失效的视频",
                        his.deleteByDiskUsage,his.deleteByDiskUsageUnit);
                pendingDeleteVideos = archiveDeleteService.queryHistoryVideosOfDiskUsageOverLimit(his.uid, his.calcDeleteByDiskUsageByte());
            }
            default -> throw new RuntimeException("非法autoDeleteMethod：" + his.autoDeleteMethod);
        }

        for (HistoryVideoInfo video : pendingDeleteVideos) {
            String bvid = video.bvid;
            //删除掉未失效的视频，保留失效的
            VideoInfo videoInfo = MiscUtils.getVideoInfoOrChangeState(biliApiService, video);
            if (videoInfo == null){
                PG.contentAndPrintf("发现历史记录视频已失效：[%s][%s]", bvid, video.title);
                video.communityUpdateTime = System.currentTimeMillis();
                videoInfoMapper.update(video);
            } else {
                PG.contentAndPrintf("正在删除历史记录视频：%s「%s」",bvid,video.title);
                videoHistoryMapper.deleteByPrimaryKey(his.uid, bvid);
                if (archiveDeleteService.deleteVideoArchiveIfNotRef(bvid)){
                    PG.contentAndPrintf("已删除历史记录视频及存档：%s「%s」",bvid,video.title);
                } else {
                    PG.contentAndPrintf("已删除历史记录视频但存档保留（因为其他备份项在使用）：%s「%s」",bvid,video.title);
                }
            }
        }
        PG.content("已完成历史记录视频备份");
    }

    private List<PendingDownloadHisVideo> getPendingDownloadHisVideos(long view_at) throws IOException, BiliBiliApiException {
        List<HistoriesPage.HistoryItem> newHistoryVideos = getVideosAfterASpecificTimePoint(view_at);
        List<PendingDownloadHisVideo> pendingDownloadHisVideos = new LinkedList<>();
        PG.content("=开始查询待下载历史记录视频=");
        for (HistoriesPage.HistoryItem historyItem : newHistoryVideos) {
            String bvid = historyItem.history.bvid;
            String title = historyItem.title;
            long authorMid = historyItem.author_mid;

            if (bvid == null || bvid.equals("")) {
                PG.contentAndPrintf("[-][番剧][%s][%s]", bvid, title);
                continue;
            }
            if (blackListUidSet.contains(authorMid)) {
                PG.contentAndPrintf("[-][UP主黑名单:%s(%s)][%s][%s]", historyItem.author_name, authorMid, bvid, title);
                continue;
            }

            GeneralResponse<VideoInfo> resp = biliApiService.getVideoInfoByBvid(bvid).exe();
            if (resp.isSuccess()) {
                VideoInfo videoInfo = resp.data;
                if (his.releaseTimeLimitDay != null && his.releaseTimeLimitDay != -1) {
                    long time = System.currentTimeMillis() / 1000 - ((his.releaseTimeLimitDay) * 24 * 60 * 60);
                    if (videoInfo.ctime < time) {
                        PG.contentAndPrintf("[-][发布时间过老(%s)][%s][%s]",
                                MiscUtils.cnSdf.format(videoInfo.ctime * 1000), bvid, title);
                        continue;
                    }
                }
                if (videoInfo.is_upower_preview) {//充电专属视频过滤掉不备份（你甚至收藏了就等着被异常阻塞进度吧）
                    PG.contentAndPrintf("[-][充电专属视频][%s][%s]", bvid, title);
                    continue;
                }
                if (videoHistoryMapper.checkExists(his.uid, videoInfo.bvid)) {
                    PG.contentAndPrintf("[=][更新观看时间][%s][%s]", bvid, title);
                    pendingDownloadHisVideos.add(new PendingDownloadHisVideo(videoInfo, historyItem, true));
                    continue;
                }
                PG.contentAndPrintf("[+][待备份][%s][%s]", bvid, title);
                pendingDownloadHisVideos.add(new PendingDownloadHisVideo(videoInfo, historyItem, false));
            } else if (ArchiveVideoInfo.getStateFromApiResponseCode(resp.code) != null) {
                PG.contentAndPrintf("[-][已失效][%s][%s]", bvid, title);
                ArchiveVideoInfo archiveVideoInfo = videoInfoMapper.selectByBvid(bvid);
                //若收藏夹的视频在备份前失效无法备份，保存了失效档案，历史记录包含封面与标题，那这里继续补充失效档案的标题和封面
                if (archiveVideoInfo != null && Objects.equals(archiveVideoInfo.state, ArchiveVideoInfo.STATE_FAILED_AND_NO_BACKUP)) {
                    String coverUrl = historyItem.cover;
                    PG.contentAndPrintf("正在为收藏夹补充失效且未存档视频的标题、封面……");
                    videoInfoMapper.supplementaryFailedVideoInfo(bvid, historyItem.title+"[由历史记录补充]", historyItem.tag_name, coverUrl);
                    userContext.videoDownloader.downloadCover(coverUrl, bvid);
                }
            } else {
                throw new RuntimeException("无法判断视频是否失效：message=" + resp.message + " code=" + resp.code);
            }
        }
        return pendingDownloadHisVideos;
    }

    /**
     * 获取该时间点往前的视频
     *
     * @param viewAt 时间戳
     * @return 历史记录视频列表
     */
    private List<HistoriesPage.HistoryItem> getVideosAfterASpecificTimePoint(long viewAt) throws IOException, BiliBiliApiException {
        List<HistoriesPage.HistoryItem> newHistoryItemList = new LinkedList<>();
        HistoriesPage page = biliApiService.getLatestVideoHistories(20).success();
        while (true) {
            for (HistoriesPage.HistoryItem historyItem : page.list) {
                if (historyItem.view_at > viewAt) {
                    newHistoryItemList.add(historyItem);
                } else {
                    return newHistoryItemList;
                }
            }
            if (page.list.size() < 1) {
                return newHistoryItemList;
            }
            page = biliApiService.getVideoHistories(20, page.cursor.max, page.cursor.view_at).success();
        }
    }

    @Override
    public String getDesc() {
        return String.format("用户：%s(%s)的历史记录", user.name, user.uid);
    }

    private record PendingDownloadHisVideo(
            VideoInfo videoInfo, HistoriesPage.HistoryItem historyItem, boolean isUpdateViewAt) {
    }
}
