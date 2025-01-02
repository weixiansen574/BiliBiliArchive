package top.weixiansen574.bilibiliArchive.core;

import top.weixiansen574.bilibiliArchive.core.backup.VideoBackup;
import top.weixiansen574.bilibiliArchive.core.operation.exception.ExceptionRecorder;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.operation.progress.ProgressBar;
import top.weixiansen574.bilibiliArchive.core.task.KeyedThreadPool;
import top.weixiansen574.bilibiliArchive.core.task.VideoBackupCall;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ContentBackupLoopThread extends Thread{
    private boolean running = true;
    private boolean jumpOver = false;
    private final long intervalSec;
    KeyedThreadPool taskPoll;
    List<VideoBackup> videoBackups;
    //TODO 准备添加 关注动态获取UP主视频，但请注意，这有很大可能会出现遗漏，原因1：你没关注UP主 原因2（有可能且致命）：限流视频给你夹动态

    public ContentBackupLoopThread(long intervalSec, KeyedThreadPool taskPoll, List<VideoBackup> videoBackups) {
        this.intervalSec = intervalSec;
        this.taskPoll = taskPoll;
        this.videoBackups = videoBackups;
    }

    @Override
    public void run() {
        PG.titleInit("定时备份器");
        if (videoBackups.size() > 0){
            while (running) {
                synchronized (this) {
                    doBackup();
                    try {
                        waitTo(intervalSec);
                    } catch (InterruptedException e) {
                        running = false;
                    }
                }
            }
        } else {
            PG.contentAndData("没有任何一个备份项启用",new ProgressBar(1,0));
            synchronized (this){
                try {
                    wait();
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }
        PG.remove();
    }

    private void waitTo(long interval) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        for (long i = 0; i < interval && !jumpOver; i++) {
            long elapsed = System.currentTimeMillis() - startTime; // 计算已消耗时间
            long remaining = interval * 1000 - elapsed; // 计算剩余时间
            if (remaining <= 0) {
                break; // 剩余时间为0或负数，直接退出
            }
            PG.contentAndData("将在 %s 后查询各个备份项的新内容",
                    new ProgressBar(interval, i),
                    MiscUtils.formatSeconds((remaining + 999) / 1000));

            long sleepTime = Math.min(1000, remaining); // 确保最后一次sleep不超过剩余时间
            wait(sleepTime); // 按剩余时间睡眠
        }
        jumpOver = false;
    }


    public void shutdown(){
        running = false;
        interrupt();
        try {
            join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 跳过循环线程的等待
     */
    public void jumpOverSleep() {
        jumpOver = true;
        synchronized (this){
            notify();
        }
    }


    private void doBackup() {
        List<VideoBackupCall> videoBackupCalls = new ArrayList<>();
        //两个列表，确保这个成功获取到了带下载视频，后面才进行提交
        List<VideoBackup> pendingCommitVBackups = new LinkedList<>();

        for (int i = 0; i < videoBackups.size(); i++) {
            VideoBackup videoBackup = videoBackups.get(i);
            PG.content("获取备份项目待下载视频：[%d/%d][%s]",i+1,videoBackups.size(),videoBackup.getDesc());
            try {
                videoBackupCalls.addAll(videoBackup.queryPendingBackupVideos());
                pendingCommitVBackups.add(videoBackup);
            } catch (Exception e) {
                ExceptionRecorder.add(e,"获取备份项目待下载视频失败，项目信息："+videoBackup.getDesc());
                PG.content("获取失败，跳过该条备份项目");
                e.printStackTrace();
            }
        }

        //有相同视频时，剔除优先低的，保留优先级最高的那个。并且按照优先级排序，优先级最高的先下
        videoBackupCalls = filterAndSort(videoBackupCalls);
        PG.content("获取到"+videoBackupCalls.size()+"个视频，下载中……");
        List<FutureTask<Void>> tasks = new ArrayList<>(videoBackupCalls.size());

        for (VideoBackupCall videoBackupCall : videoBackupCalls) {
            System.out.println("[定时备份器]下载视频："+videoBackupCall.getBvid()+"「"+videoBackupCall.getTitle()+"」");
            FutureTask<Void> futureTask = new FutureTask<>(videoBackupCall);
            tasks.add(futureTask);
            taskPoll.submit(videoBackupCall.getKey(),futureTask);
        }

        Map<String,Boolean> downloadedBvidMap = new HashMap<>();
        for (int i = 0; i < videoBackupCalls.size(); i++) {
            VideoBackupCall call = videoBackupCalls.get(i);
            String bvid = call.getBvid();
            String title = call.getTitle();

            try {
                tasks.get(i).get();
                downloadedBvidMap.put(bvid,true);
                System.out.println("[定时备份器]视频下载完成："+bvid+"「"+title+"」");
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("[定时备份器]视频下载失败："+bvid+"「"+title+"」");
                downloadedBvidMap.put(bvid,false);
            }
        }

        for (int i = 0; i < pendingCommitVBackups.size(); i++) {
            VideoBackup videoBackup = pendingCommitVBackups.get(i);
            PG.content("正在提交：[%d/%d][%s]",i+1,pendingCommitVBackups.size(),videoBackup.getDesc());
            try {
                videoBackup.commit(downloadedBvidMap);
            } catch (Exception e) {
                e.printStackTrace();
                ExceptionRecorder.add(e,"提交已下载视频到备份项目失败，项目信息："+videoBackup.getDesc());
                PG.content("提交失败：[%d/%d][%s]",i+1,pendingCommitVBackups.size(),videoBackup.getDesc());
            }
        }
        PG.content("提交完成");

    }

    private static List<VideoBackupCall> filterAndSort(List<VideoBackupCall> videos) {
        // 用Map存储每个id对应的优先级最大的VideoBackupCall
        Map<String, VideoBackupCall> videoMap = new HashMap<>();

        for (VideoBackupCall video : videos) {
            // 如果当前Map中没有该id，或者当前视频的priority更小（优先级更高）
            videoMap.putIfAbsent(video.getBvid(), video);

            // 比较当前video和已存的video，若当前video的优先级更高，则替换
            VideoBackupCall existingVideo = videoMap.get(video.getBvid());
            if (video.compareTo(existingVideo) < 0) {
                videoMap.put(video.getBvid(), video);
            }
        }

        // 返回去重并排序后的列表
        List<VideoBackupCall> result = new ArrayList<>(videoMap.values());
        Collections.sort(result);
        return result;
    }
    
    


}
