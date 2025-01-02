package top.weixiansen574.bilibiliArchive.core;

import top.weixiansen574.bilibiliArchive.bean.config.VideoContentUpdateConfig;
import top.weixiansen574.bilibiliArchive.bean.config.VideoDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.contentupdate.VideoUpdatePlan;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;
import top.weixiansen574.bilibiliArchive.core.downloaders.CommentDownloader;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.operation.progress.ProgressBar;
import top.weixiansen574.bilibiliArchive.core.task.KeyedThreadPool;
import top.weixiansen574.bilibiliArchive.core.task.VideoUpdateTask;
import top.weixiansen574.bilibiliArchive.mapper.master.ContentUpdatePlanMapper;
import top.weixiansen574.bilibiliArchive.services.UserService;

import java.util.List;

public class ContentUpdateThread extends Thread {
    boolean isRun = true;
    ContentUpdatePlanMapper mapper;
    KeyedThreadPool taskPoll;
    UserService userService;

    public ContentUpdateThread(ContentUpdatePlanMapper mapper, KeyedThreadPool taskPoll, UserService userService) {
        this.mapper = mapper;
        this.taskPoll = taskPoll;
        this.userService = userService;
    }

    @Override
    public void run() {
        PG.titleInit("内容更新器");
        while (isRun) {
            try {
                VideoUpdatePlan plan = mapper.selectEarliestVideoUpdatePlan();
                if (plan == null) {
                    synchronized (this) {
                        PG.contentAndData("当前无任务",new ProgressBar(1,0));
                        wait();
                    }
                } else {
                    long sleepTime = plan.timestamp - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        waitTo(sleepTime,plan);
                        UserContext userContext = userService.getUserContext(plan.uid);
                        if (userContext == null) {
                            PG.content("未找到UID：" + plan.uid + " 无法执行更新任务");
                        } else {
                            taskPoll.submit(plan.bvid,new VideoUpdateTask(this,userContext,plan));
                        }
                    } else {
                        PG.content("更新任务：" + plan + "已过期");
                    }
                    mapper.deleteVideoUpdatePlanById(plan.id);
                }
            } catch (InterruptedException ignored) {
            }
        }
        PG.remove();
    }

    private void waitTo(long interval,VideoUpdatePlan plan) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + interval;

        for (long currentTime = startTime; currentTime < endTime; currentTime = System.currentTimeMillis()) {
            long timeLeft = endTime - currentTime;

            if (timeLeft > 1000) {
                // 等待1秒
                Thread.sleep(1000);
            } else {
                // 如果剩余时间不足1秒，直接等待剩余时间
                Thread.sleep(timeLeft);
            }

            // 打印日志
            PG.contentAndData("将在%s后更新视频：[%s][%s]", new ProgressBar(interval, interval-timeLeft),
                    formatTime(timeLeft), plan.bvid, plan.title);
        }
    }

    public synchronized void removeVideoUpdatePlans(String bvid) {
        mapper.deleteVideoUpdatePlanByBvid(bvid);
    }

    public void addVideoUpdatePlan(Long uid,String bvid,Long avid,String title,long startTime,VideoDownloadConfig videoConfig, List<VideoContentUpdateConfig> updateConfigs){
        if (updateConfigs == null) {
            return;
        }
        //清理掉bvid相重的更新计划（优先级覆盖时避免之前的任务窜扰）
        mapper.deleteVideoUpdatePlanByBvid(bvid);
        VideoUpdatePlan plan = new VideoUpdatePlan();
        plan.uid = uid;
        plan.bvid = bvid;
        plan.avid = avid;
        plan.title = title;
        plan.videoCodecId = videoConfig.codecId;
        plan.videoQuality = videoConfig.clarity;

        for (VideoContentUpdateConfig updateConfig : updateConfigs) {
            plan.updateVideoAndDanmaku = updateConfig.updateVideoAndDanmaku;
            int spacing2 = 0;
            int spacing3 = 0; // 初始化 spacing3
            for (int i = 1; i <= updateConfig.loopCount; i++) {
                plan.timestamp = startTime + (updateConfig.intervalToMs() * i);
                // 比当前时间小的不添加
                if (plan.timestamp < System.currentTimeMillis()) {
                    continue;
                }
                plan.commentConfig1 = updateConfig.comment1;
                // 首先评论更新配置1要不为null，二次更新才有效
                if (updateConfig.comment1 != null) {
                    if (updateConfig.comment2 != null) {
                        if (spacing2 == updateConfig.comment2Spacing) {
                            plan.commentConfig2 = updateConfig.comment2;
                            spacing2 = 0;
                        } else {
                            plan.commentConfig2 = null;
                            spacing2 += 1;
                        }
                    }
                }

                // 评论更新配置1要不为null，三次更新才有效
                if (updateConfig.comment1 != null) {
                    if (updateConfig.comment3 != null) {
                        if (spacing3 == updateConfig.comment3Spacing) {
                            plan.commentConfig3 = updateConfig.comment3;
                            plan.commentConfig2 = null;//若commentConfig3生效，commentConfig2将失效
                            spacing3 = 0;
                        } else {
                            plan.commentConfig3 = null;
                            spacing3 += 1;
                        }
                    }
                }
                mapper.insertVideoUpdatePlan(plan);
            }
            startTime = plan.timestamp;
        }
        //中断sleep，使其重新查询
        interrupt();
    }

    public void addVideoUpdatePlan(Long uid, VideoInfo videoInfo, VideoDownloadConfig videoConfig, List<VideoContentUpdateConfig> updateConfigs) {
        addVideoUpdatePlan(uid,videoInfo.bvid, videoInfo.aid, videoInfo.title,videoInfo.ctime * 1000,videoConfig,updateConfigs);
    }

    public void pushTask(UserContext userContext,VideoUpdatePlan plan){
        taskPoll.submit(plan.bvid,new VideoUpdateTask(this,userContext,plan));
    }

    private static String formatTime(long millis) {
        long days = millis / 86400000;
        long hours = (millis % 86400000) / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;

        if (days > 0) {
            return String.format("%d天%02d时%02d分%02d秒", days, hours, minutes, seconds);
        } else {
            return String.format("%02d时%02d分%02d秒", hours, minutes, seconds);
        }
    }

    public void shutdown(){
        isRun = false;
        interrupt();
        try {
            join();
        } catch (InterruptedException ignored) {
        }
    }

}
