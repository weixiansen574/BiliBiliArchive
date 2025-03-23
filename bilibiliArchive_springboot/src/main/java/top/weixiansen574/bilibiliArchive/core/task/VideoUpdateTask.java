package top.weixiansen574.bilibiliArchive.core.task;

import top.weixiansen574.bilibiliArchive.bean.VideoMetadataLog;
import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.contentupdate.VideoUpdatePlan;
import top.weixiansen574.bilibiliArchive.bean.list.MetadataChangeList;
import top.weixiansen574.bilibiliArchive.bean.list.VideoPageVersionList;
import top.weixiansen574.bilibiliArchive.bean.list.VideoTagList;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.DownloadedVideoPage;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.VideoPageVersion;
import top.weixiansen574.bilibiliArchive.core.ContentUpdateThread;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.GeneralResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;
import top.weixiansen574.bilibiliArchive.core.downloaders.VideoDownloader;
import top.weixiansen574.bilibiliArchive.core.operation.exception.ExceptionRecorder;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoInfoMapper;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class VideoUpdateTask implements Runnable {

    ContentUpdateThread updateThread;
    UserContext userContext;
    VideoUpdatePlan plan;

    public VideoUpdateTask(ContentUpdateThread updateThread, UserContext userContext, VideoUpdatePlan plan) {
        this.updateThread = updateThread;
        this.userContext = userContext;
        this.plan = plan;
    }

    @Override
    public void run() {
        PG.titleInit("视频更新·" + plan.bvid + "「" + plan.title + "」");
        VideoDownloader videoDownloader = userContext.videoDownloader;
        VideoInfoMapper videoInfoMapper = userContext.videoInfoMapper;
        try {
            ArchiveVideoInfo archiveVideoInfo = videoInfoMapper.selectByBvid(plan.bvid);
            String bvid = plan.bvid;
            String title = plan.title;
            if (archiveVideoInfo == null) {
                //移除任务
                updateThread.removeVideoUpdatePlans(bvid);
                PG.content("视频：" + bvid + " 已被移出存档库，已取消该视频所有更新任务");
                pgRemove();
                return;
            }
            //更新视频
            VideoInfo videoInfo = MiscUtils.getVideoInfoOrChangeState(userContext, archiveVideoInfo);
            if (videoInfo == null) {
                //archiveVideoInfo.communityUpdateTime = System.currentTimeMillis();
                videoInfoMapper.update(archiveVideoInfo);
                PG.content("发现视频[%s][%s]失效，未执行更新，已更新存档状态。保留对此视频的更新任务列表，以处理可能视频会复活的情况",
                        bvid, title);
                pgRemove();
                return;
            }
            //如果视频的观看次数没有变化就不要更新了，因为观看量重复观看一次也会导致播放量+1，即使是UP主也会！
            //用户进来发条评论、弹幕、点赞评论，必然会导致视频被观看从而播放量+1。
            //可以通过判断播放量是否发生变化来决定是否更新，从而避免时间和流量的浪费
            if (videoInfo.stat.view == archiveVideoInfo.view) {
                updateVideoInfo(videoInfoMapper,archiveVideoInfo,videoInfo);
                return;
            }
            //archiveVideoInfo.state = ArchiveVideoInfo.STATE_NORMAL;//如果它之前失效但是现在复活了
            PG.content("正在更新视频项目[%s][%s]……", bvid, title);
            CommentDownloadConfig comment1 = plan.commentConfig1;
            CommentDownloadConfig comment2 = plan.commentConfig2;
            CommentDownloadConfig comment3 = plan.commentConfig3;

            //hmm…… 评论不能通过这种方式来节约，最主要是因为要更新点赞数那些的
            //当然还有用户删除评论、评论被阿瓦隆秋后算账都会导致评论数量减少，然后刚好这时又有人发了条新的数量加起来又一样了
            //或者可以通过设置决定？
            if (comment1 != null) {
                PG.content("正在更新评论……");
                int totalFloor = videoDownloader.downloadOrUpdateComment(videoInfo.aid, VideoDownloader.TYPE_VIDEO, comment1);
                if (totalFloor != 0) {
                    archiveVideoInfo.totalCommentFloor = totalFloor;
                }
                PG.content("评论更新完成");
            }

            if (comment2 != null) {
                PG.content("正在二次更新评论……");
                videoDownloader.downloadOrUpdateComment(videoInfo.aid, VideoDownloader.TYPE_VIDEO, comment2);
            }

            if (comment3 != null) {
                PG.content("正在三次更新评论……");
                videoDownloader.downloadOrUpdateComment(videoInfo.aid, VideoDownloader.TYPE_VIDEO, comment3);
            }
            MetadataChangeList metadataChanges = archiveVideoInfo.metadataChanges;
            //元数据被修改的更新
            if (metadataChanges.size() > 0) {
                VideoMetadataLog log = metadataChanges.get(metadataChanges.size() - 1);
                if (!Objects.equals(log.title, videoInfo.title) || !Objects.equals(log.desc, videoInfo.desc)
                        || !Objects.equals(MiscUtils.getEndPathForHttpUrl(log.coverUrl),
                        MiscUtils.getEndPathForHttpUrl(videoInfo.pic))) {
                    videoDownloader.downloadCoverToChangedIfNotExists(videoInfo.pic, bvid);
                    VideoTagList videoTags = videoDownloader.getVideoTags(bvid);
                    metadataChanges.add(new VideoMetadataLog(videoInfo.title, videoInfo.desc, videoInfo.pic, videoTags,
                            System.currentTimeMillis()));
                }
            } else {
                if (!Objects.equals(archiveVideoInfo.title, videoInfo.title)
                        || !Objects.equals(archiveVideoInfo.desc, videoInfo.desc)
                        || !Objects.equals(MiscUtils.getEndPathForHttpUrl(archiveVideoInfo.coverUrl),
                        MiscUtils.getEndPathForHttpUrl(videoInfo.pic))) {
                    videoDownloader.downloadCoverToChangedIfNotExists(videoInfo.pic, bvid);
                    VideoTagList videoTags = videoDownloader.getVideoTags(bvid);
                    metadataChanges.add(new VideoMetadataLog(videoInfo.title, videoInfo.desc, videoInfo.pic, videoTags,
                            System.currentTimeMillis()));
                }
            }
            if (plan.updateVideoAndDanmaku) {
                PG.content("正在更新视频……");
                VideoPageVersion videoPageVersion = archiveVideoInfo.latestPageVersion();
                assert videoPageVersion != null;
                UserContext vContext = userContext;
                //如果用户不是大会员，且设置了公共大会员账号，视频部分将由公共大会员账号下载
                if (!userContext.biliUser.currentTimeIsVip() || userContext.getPublicVipUserContext() != null) {
                    vContext = userContext.getPublicVipUserContext();
                }
                List<DownloadedVideoPage> downloadedPages = vContext.videoDownloader
                        .downloadOrUpdateVideo(videoInfo, videoPageVersion.pages, plan.videoQuality, plan.videoCodecId,
                                320 * 1024, vContext.biliUser.currentTimeIsVip());
                //更新剧集
                if (archiveVideoInfo.addToPageVersionsIfInconsistency(downloadedPages)) {
                    PG.content("视频更新完成，检测到视频剧集列表发生变化，已更新！");
                } else {
                    PG.content("视频未更新，剧集列表未发生变化");
                }
                //如果弹幕数没有变化就不更新弹幕
                if (archiveVideoInfo.danmaku != videoInfo.stat.danmaku){
                    PG.content("正在更新弹幕……");
                    videoDownloader.downloadOrUpdateDanmaku(videoInfo);
                    PG.content("弹幕更新完成");
                }
            }
            updateVideoInfo(videoInfoMapper,archiveVideoInfo,videoInfo);
            PG.content("更新完成");
            PG.remove();
        } catch (Exception e) {
            e.printStackTrace();
            PG.content("发生异常，更新失败，异常信息：" + e);
            ExceptionRecorder.add(e, "更新视频" + plan.bvid + "「" + plan.title + "」时出错");
            pgRemove();
        }
    }

    private static void pgRemove() {
        MiscUtils.sleepNoException(5000);
        PG.remove();
    }

    private void updateVideoInfo(VideoInfoMapper videoInfoMapper, ArchiveVideoInfo archiveVideoInfo, VideoInfo videoInfo) {
        archiveVideoInfo.communityUpdateTime = System.currentTimeMillis();
        archiveVideoInfo.duration = videoInfo.duration;
        archiveVideoInfo.view = videoInfo.stat.view;
        archiveVideoInfo.danmaku = videoInfo.stat.danmaku;
        archiveVideoInfo.favorite = videoInfo.stat.favorite;
        archiveVideoInfo.coin = videoInfo.stat.coin;
        archiveVideoInfo.like = videoInfo.stat.like;
        archiveVideoInfo.share = videoInfo.stat.share;
        archiveVideoInfo.reply = videoInfo.stat.reply;
        videoInfoMapper.update(archiveVideoInfo);
    }
}
