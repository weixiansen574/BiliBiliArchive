package top.weixiansen574.bilibiliArchive.core.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.weixiansen574.bilibiliArchive.bean.BiliUser;
import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;
import top.weixiansen574.bilibiliArchive.bean.VideoMetadataLog;
import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.config.VideoDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.list.MetadataChangeList;
import top.weixiansen574.bilibiliArchive.bean.list.VideoTagList;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.DownloadedVideoPage;
import top.weixiansen574.bilibiliArchive.core.ContentUpdateThread;
import top.weixiansen574.bilibiliArchive.core.PriorityManger;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;
import top.weixiansen574.bilibiliArchive.core.downloaders.CommentDownloader;
import top.weixiansen574.bilibiliArchive.core.downloaders.VideoDownloader;
import top.weixiansen574.bilibiliArchive.core.operation.exception.ExceptionRecorder;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoInfoMapper;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class VideoBackupCall implements Comparable<VideoBackupCall>, KeyedCall<Void> {
    private final VideoInfo videoInfo;

    private final PriorityManger priorityManger;
    private final ContentUpdateThread contentUpdateThread;
    private final VideoBackupConfig config;

    public final int priority;
    private final VideoDownloader videoDownloader;
    private final VideoInfoMapper videoInfoMapper;
    private final UserContext userContext;
    private final BiliUser biliUser;

    public VideoBackupCall(VideoInfo videoInfo, UserContext userContext, PriorityManger priorityManger, ContentUpdateThread contentUpdateThread, VideoBackupConfig videoBackupConfig) {
        this.videoInfo = videoInfo;
        this.priorityManger = priorityManger;
        this.contentUpdateThread = contentUpdateThread;
        this.config = videoBackupConfig;
        this.videoInfoMapper = userContext.videoInfoMapper;
        this.videoDownloader = userContext.videoDownloader;
        this.userContext = userContext;
        this.biliUser = userContext.biliUser;
        this.priority = priorityManger.getVideoCfgPriority(videoBackupConfig.id);
    }

    @Override
    public Void call() throws Exception {
        PG.titleInit("视频备份·" + videoInfo.bvid + "「" + videoInfo.title + "」");
        //System.out.printf("正在备份视频%s(%s)……%n", videoInfo.bvid, videoInfo.title);
        try {
            boolean downloaded = downloadOrOverwriteVideoArchive(videoInfo, config.video, config.comment);
            //System.out.printf("视频%s(%s)备份完成！%n", videoInfo.bvid, videoInfo.title);
            if (downloaded && contentUpdateThread != null) {
                PG.content("正在添加更新任务……");
                contentUpdateThread.addVideoUpdatePlan(biliUser.uid, videoInfo, config.video, config.update);
            }
            PG.remove();
        } catch (Exception e) {
            PG.content("视频下载失败，因为：" + e);
            ExceptionRecorder.add(e, "下载视频" + videoInfo.bvid + "「" + videoInfo.title + "」时出错");
            Thread.sleep(5000);
            PG.remove();
            throw e;
        }
        return null;
    }

    /**
     * 下载或者按优先级覆盖原有存档的视频
     *
     * @return 是否是新下载或覆盖原有的？
     */
    public boolean downloadOrOverwriteVideoArchive(VideoInfo videoInfo, VideoDownloadConfig videoCfg, CommentDownloadConfig commentCfg)
            throws BiliBiliApiException, IOException {
        ArchiveVideoInfo archiveVideoInfo = videoInfoMapper.selectByBvid(videoInfo.bvid);
        if (archiveVideoInfo != null) {
            if (archiveVideoInfo.downloading != 0) {
                //上次下载被中断，无论优先级多少，都进行下载或更新
                downloadVideoAndAdditions(archiveVideoInfo, videoInfo, videoCfg, commentCfg);
                archiveVideoInfo.communityUpdateTime = System.currentTimeMillis();
                videoInfoMapper.update(archiveVideoInfo);
                return true;
            } else if (checkIfOverwriteNeeded(archiveVideoInfo)) {
                //上次的优比本次的先级低，覆盖下载
                downloadVideoAndAdditions(archiveVideoInfo, videoInfo, videoCfg, commentCfg);
                archiveVideoInfo.communityUpdateTime = System.currentTimeMillis();
                videoInfoMapper.update(archiveVideoInfo);
                return true;
            } else {
                //上次的优先级更高，跳过下载
                return false;
            }
        }
        //全新下载
        archiveVideoInfo = insertNewDownloadingArcVideoInfo(videoInfo, config.id);
        downloadVideoAndAdditions(archiveVideoInfo, videoInfo, videoCfg, commentCfg);
        videoInfoMapper.update(archiveVideoInfo);
        return true;
    }

    protected boolean checkIfOverwriteNeeded(ArchiveVideoInfo archiveVideoInfo) {
        return priorityManger.getVideoCfgPriority(archiveVideoInfo.configId) >
                priorityManger.getVideoCfgPriority(config.id);
    }

    private void downloadVideoAndAdditions
            (ArchiveVideoInfo archiveVideoInfo, VideoInfo videoInfo, VideoDownloadConfig video,
             CommentDownloadConfig commentCfg) throws IOException, BiliBiliApiException {
        String bvid = videoInfo.bvid;
        //更新视频下载状态为正在下载
        videoInfoMapper.updateDownloading(bvid, ArchiveVideoInfo.DOWNLOAD_STATE_DOWNLOADING);
        //下载up主头像
        videoDownloader.downloadUpAvatarIfNotExists(archiveVideoInfo.ownerAvatarUrl);
        //下载合作用户列表中的用户头像
        videoDownloader.downloadStaffAvatarsIfNotExists(bvid,archiveVideoInfo.staff);
        //下载封面
        videoDownloader.downloadCoverIfNotExists(archiveVideoInfo.coverUrl, bvid);
        //如果视频元信息与预期的不一致说明被修改了，将新的信息添加到修改记录里，不改变当前视频的元数据
        VideoTagList videoTags = videoDownloader.getVideoTags(bvid);
        if (archiveVideoInfo.tags == null) {
            archiveVideoInfo.tags = videoTags;
        }

        MetadataChangeList metadataChanges = archiveVideoInfo.metadataChanges;
        if (metadataChanges.size() > 0) {
            //若元数据日志与上个版本发生变化，新增到修改记录里
            VideoMetadataLog log = metadataChanges.get(metadataChanges.size() - 1);
            if (!Objects.equals(log.title, videoInfo.title) || !Objects.equals(log.desc, videoInfo.desc)
                    || !Objects.equals(MiscUtils.getEndPathForHttpUrl(log.coverUrl),
                    MiscUtils.getEndPathForHttpUrl(videoInfo.pic))) {
                videoDownloader.downloadCoverToChangedIfNotExists(videoInfo.pic,bvid);
                metadataChanges.add(new VideoMetadataLog(videoInfo.title, videoInfo.desc, videoInfo.pic, videoTags,
                        System.currentTimeMillis()));
            }
        } else {
            if (!Objects.equals(archiveVideoInfo.title, videoInfo.title)
                    || !Objects.equals(archiveVideoInfo.desc, videoInfo.desc)
                    || !Objects.equals(MiscUtils.getEndPathForHttpUrl(archiveVideoInfo.coverUrl),
                    MiscUtils.getEndPathForHttpUrl(videoInfo.pic))) {
                videoDownloader.downloadCoverToChangedIfNotExists(videoInfo.pic,bvid);
                metadataChanges.add(new VideoMetadataLog(videoInfo.title, videoInfo.desc, videoInfo.pic, videoTags,
                        System.currentTimeMillis()));
            }
        }
        List<DownloadedVideoPage> latestPages = archiveVideoInfo.latestPages();
        //下载视频
        UserContext vContext = userContext;
        //如果用户不是大会员，且设置了公共大会员账号，视频部分将由公共大会员账号下载
        if (!biliUser.currentTimeIsVip() || userContext.getPublicVipUserContext() != null) {
            vContext = userContext.getPublicVipUserContext();
        }
        List<DownloadedVideoPage> downloadedPages = vContext.videoDownloader.downloadOrUpdateVideo(videoInfo, latestPages,
                video.clarity, video.codecId, 320 * 1024, vContext.biliUser.currentTimeIsVip());
        //下载弹幕
        videoDownloader.downloadOrUpdateDanmaku(videoInfo);
        //保存视频标签信息

        //下载评论
        archiveVideoInfo.totalCommentFloor = videoDownloader
                .downloadOrUpdateComment(videoInfo.aid, CommentDownloader.TYPE_VIDEO, commentCfg);

        archiveVideoInfo.configId = config.id;
        archiveVideoInfo.downloading = ArchiveVideoInfo.DOWNLOAD_STATE_OK;
        archiveVideoInfo.addToPageVersionsIfInconsistency(downloadedPages);
    }

    public ArchiveVideoInfo insertNewDownloadingArcVideoInfo(VideoInfo videoInfo, int config_id) {
        long time = System.currentTimeMillis();
        ArchiveVideoInfo archiveVideoInfo = new ArchiveVideoInfo(videoInfo, null,
                ArchiveVideoInfo.STATE_NORMAL, time, config_id, time,
                ArchiveVideoInfo.DOWNLOAD_STATE_DOWNLOADING);
        videoInfoMapper.insert(archiveVideoInfo);
        return archiveVideoInfo;
    }

    @Override
    public int compareTo(@NotNull VideoBackupCall other) {
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public String getKey() {
        return getBvid();
    }

    public String getBvid() {
        return videoInfo.bvid;
    }

    public String getTitle() {
        return videoInfo.title;
    }
}
