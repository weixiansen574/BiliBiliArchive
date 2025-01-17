package top.weixiansen574.bilibiliArchive.core.task;

import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.contentupdate.VideoUpdatePlan;
import top.weixiansen574.bilibiliArchive.bean.list.VideoPageVersionList;
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

public class VideoUpdateTask implements Runnable{

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
        PG.titleInit("视频更新·"+plan.bvid+"「"+plan.title+"」");
        VideoDownloader videoDownloader = userContext.videoDownloader;
        BiliApiService biliApiService = userContext.biliApiService;
        VideoInfoMapper videoInfoMapper = userContext.videoInfoMapper;
        try {
            ArchiveVideoInfo archiveVideoInfo = videoInfoMapper.selectByBvid(plan.bvid);
            String bvid = plan.bvid;
            String title = plan.title;
            if (archiveVideoInfo == null) {
                //移除任务
                updateThread.removeVideoUpdatePlans(bvid);
                PG.content("视频：" + bvid + " 已被移出存档库，已取消更新任务");
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
            archiveVideoInfo.state = ArchiveVideoInfo.STATE_NORMAL;//如果它之前失效但是现在复活了
            PG.content("正在更新视频项目[%s][%s]……", bvid, title);
            CommentDownloadConfig comment1 = plan.commentConfig1;
            CommentDownloadConfig comment2 = plan.commentConfig2;
            CommentDownloadConfig comment3 = plan.commentConfig3;

            if (comment1 != null){
                PG.content("正在更新评论……");
                int totalFloor = videoDownloader.downloadOrUpdateComment(videoInfo.aid, VideoDownloader.TYPE_VIDEO, comment1);
                if (totalFloor != 0){
                    archiveVideoInfo.totalCommentFloor = totalFloor;
                }
                PG.content("评论更新完成");
            }

            if(comment2 != null){
                PG.content("正在二次更新评论……");
                videoDownloader.downloadOrUpdateComment(videoInfo.aid,VideoDownloader.TYPE_VIDEO,comment2);
            }

            if (comment3 != null){
                PG.content("正在三次更新评论……");
                videoDownloader.downloadOrUpdateComment(videoInfo.aid,VideoDownloader.TYPE_VIDEO,comment3);
            }


            if (plan.updateVideoAndDanmaku) {
                PG.content("正在更新视频……");
                VideoPageVersionList pageVersions = archiveVideoInfo.pagesVersionList;
                int size = pageVersions.size();
                VideoPageVersion videoPageVersion = pageVersions.get(size - 1);
                List<DownloadedVideoPage> videoPages = videoDownloader.downloadOrUpdateVideo(videoInfo,
                        videoPageVersion.pages,
                        plan.videoQuality, plan.videoCodecId,
                        320 * 1024, userContext.biliUser.currentTimeIsVip());
                ArchiveVideoInfo.addToLastIfInconsistency(pageVersions, videoPages);
                //更新剧集
                archiveVideoInfo.pagesVersionList = pageVersions;
                if (size != pageVersions.size()) {
                    PG.content("视频更新完成，检测到视频剧集列表发生变化，已更新！剧集列表版本信息 " +
                            MiscUtils.cnSdf.format(new Date(pageVersions.get(size - 1).versionTime)) + " => " +
                            MiscUtils.cnSdf.format(new Date(pageVersions.get(size).versionTime)));
                } else {
                    PG.content("视频未更新，剧集列表未发生变化");
                }
                PG.content("正在更新弹幕……");
                videoDownloader.downloadOrUpdateDanmaku(videoInfo);
                PG.content("弹幕更新完成");
            }
            archiveVideoInfo.communityUpdateTime = System.currentTimeMillis();
            archiveVideoInfo.view = videoInfo.stat.view;
            archiveVideoInfo.danmaku = videoInfo.stat.danmaku;
            archiveVideoInfo.favorite = videoInfo.stat.favorite;
            archiveVideoInfo.coin = videoInfo.stat.coin;
            archiveVideoInfo.like = videoInfo.stat.like;
            archiveVideoInfo.share = videoInfo.stat.share;
            archiveVideoInfo.reply = videoInfo.stat.reply;
            videoInfoMapper.update(archiveVideoInfo);
            PG.content("更新完成");
            PG.remove();
        } catch (Exception e) {
            e.printStackTrace();
            PG.content("发生异常，更新失败，异常信息："+e);
            ExceptionRecorder.add(e,"更新视频"+plan.bvid+"「"+plan.title+"」时出错");
            pgRemove();
        }
    }

    private static void pgRemove(){
        MiscUtils.sleepNoException(5000);
        PG.remove();
    }
}
