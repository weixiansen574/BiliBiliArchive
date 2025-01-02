package top.weixiansen574.bilibiliArchive.core.backup;

import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;
import top.weixiansen574.bilibiliArchive.core.ContentUpdateThread;
import top.weixiansen574.bilibiliArchive.core.PriorityManger;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.task.VideoBackupCall;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoInfoMapper;

import java.util.List;
import java.util.Map;

public abstract class VideoBackup implements Comparable<VideoBackup> {

    protected UserContext userContext;
    protected PriorityManger priorityManger;
    protected ContentUpdateThread updateThread;
    protected VideoBackupConfig videoBackupConfig;

    protected VideoInfoMapper videoInfoMapper;

    protected BiliApiService biliApiService;
    protected int priority;

    //用户上下文 优先级管理器 更新任务管理/线程 视频备份配置
    public VideoBackup(UserContext userContext, PriorityManger priorityManger, ContentUpdateThread updateThread, VideoBackupConfig videoBackupConfig) {
        this.userContext = userContext;
        this.priorityManger = priorityManger;
        this.updateThread = updateThread;
        this.videoBackupConfig = videoBackupConfig;

        videoInfoMapper = userContext.videoInfoMapper;
        biliApiService = userContext.biliApiService;

        priority = priorityManger.getVideoCfgPriority(videoBackupConfig.id);
    }

    public abstract List<VideoBackupCall> queryPendingBackupVideos() throws Exception;
    public abstract void commit(Map<String,Boolean> downloadedBvidMap) throws Exception;
    public abstract String getDesc();

    @Override
    public int compareTo(VideoBackup other) {
        return Integer.compare(this.priority, other.priority);
    }
}
