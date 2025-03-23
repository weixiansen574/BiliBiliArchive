package top.weixiansen574.bilibiliArchive.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupFav;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupHistory;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupUploader;
import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.config.VideoContentUpdateConfig;
import top.weixiansen574.bilibiliArchive.bean.config.VideoDownloadConfig;
import top.weixiansen574.bilibiliArchive.bean.contentupdate.VideoUpdatePlan;
import top.weixiansen574.bilibiliArchive.bean.list.VideoContentUpdateConfigList;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;
import top.weixiansen574.bilibiliArchive.core.*;
import top.weixiansen574.bilibiliArchive.core.backup.FavVideoBackup;
import top.weixiansen574.bilibiliArchive.core.backup.HistoryVideoBackup;
import top.weixiansen574.bilibiliArchive.core.backup.UploaderVideoBackup;
import top.weixiansen574.bilibiliArchive.core.backup.VideoBackup;
import top.weixiansen574.bilibiliArchive.core.task.*;
import top.weixiansen574.bilibiliArchive.core.util.JSONConfig;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.core.util.OkHttpUtil;
import top.weixiansen574.bilibiliArchive.exceptions.RuntimeConfigurationEditException;
import top.weixiansen574.bilibiliArchive.mapper.master.*;
import top.weixiansen574.bilibiliArchive.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Exchanger;

@Service
public class BackupService {

    private BackupSettings settings;
    private final File backupSettingsFile;

    private final UserService userService;
    private final VideoBackupConfigMapper videoBackupConfigMapper;
    private final BackupFavMapper backupFavMapper;
    private final BackupHistoryMapper backupHistoryMapper;
    private final BackupUploaderMapper backupUploaderMapper;
    private final ContentUpdatePlanMapper contentUpdatePlanMapper;
    private final ArchiveDeleteService archiveDeleteService;


    ContentUpdateThread updateThread;
    ContentBackupLoopThread backupLoopThread;
    PriorityManger priorityManger;

    KeyedThreadPool taskPoll;

    private Long runAt;

    private Thread operateThread;

    @Autowired
    public BackupService(@Value("${files.backup-settings-file}") File backupSettingsFile, UserService userService,
                         VideoBackupConfigMapper videoBackupConfigMapper, BackupFavMapper backupFavMapper,
                         BackupHistoryMapper backupHistoryMapper, BackupUploaderMapper backupUploaderMapper,
                         ContentUpdatePlanMapper contentUpdatePlanMapper, ArchiveDeleteService archiveDeleteService)
            throws IOException, PriorityListMismatchException {
        this.userService = userService;
        this.videoBackupConfigMapper = videoBackupConfigMapper;
        this.backupFavMapper = backupFavMapper;
        this.backupHistoryMapper = backupHistoryMapper;
        this.backupUploaderMapper = backupUploaderMapper;

        this.backupSettingsFile = backupSettingsFile;
        this.contentUpdatePlanMapper = contentUpdatePlanMapper;
        this.archiveDeleteService = archiveDeleteService;
        this.settings = readBackupSettings();
        checkVideoBackupPriorityList(settings.videoBackupPriorityList);
        priorityManger = new PriorityManger(settings.videoBackupPriorityList);
        userService.setGlobalVipUser(settings.publicVipUserId);
    }

    public synchronized void start(){
        if (isRun()){
            return;
        }
        taskPoll = new KeyedThreadPool(settings.taskPoolSize);

        updateThread = new ContentUpdateThread(contentUpdatePlanMapper,taskPoll,userService);

        List<BackupFav> backupFavList = backupFavMapper.selectAllEnabled();
        List<BackupHistory> backupsHistoriesList = backupHistoryMapper.selectAllEnabled();
        List<BackupUploader> backupUploaderList = backupUploaderMapper.selectAllVideoEnabled();

        List<VideoBackup> videoBackups = new LinkedList<>();

        for (BackupFav backupFav : backupFavList) {
            videoBackups.add(new FavVideoBackup(userService.getUserContext(backupFav.backupUserId),priorityManger,updateThread,
                    backupFav));
        }

        for (BackupHistory backupHistory : backupsHistoriesList){
            videoBackups.add(new HistoryVideoBackup(userService.getUserContext(backupHistory.uid),priorityManger,updateThread,
                    backupHistory,settings.globalUpBlackList));
        }

        for (BackupUploader backupUploader : backupUploaderList) {
            videoBackups.add(new UploaderVideoBackup(userService.getUserContext(backupUploader.backupUserId),priorityManger,
                    updateThread,backupUploader));
        }

        updateThread.start();
        backupLoopThread = new ContentBackupLoopThread(settings.intervalMinuteOfLoop * 60,taskPoll,videoBackups);
        backupLoopThread.start();
        runAt = System.currentTimeMillis();
        System.out.println("启动备份");
    }

    public synchronized void stop(){
        if (isRun()){
            backupLoopThread.shutdown();
            updateThread.shutdown();
            taskPoll.shutdown();
            backupLoopThread = null;
            updateThread = null;
            taskPoll = null;
            runAt = null;
            System.out.println("停止备份");
        }
    }

    public boolean isRun(){
        return taskPoll != null;
    }

    public Long getRunAt() {
        return runAt;
    }

    /**
     * 检查视频备份优先级ID列表是否在数据库中都存在
     */
    private void checkVideoBackupPriorityList(List<Integer> videoBackupPriorityList) throws PriorityListMismatchException {
        List<VideoBackupConfig> allVideoBackupConfig = videoBackupConfigMapper.selectAll();
        if (allVideoBackupConfig.size() != videoBackupPriorityList.size()) {
            throw new PriorityListMismatchException("视频配置id优先级列表与数据库中的不匹配！");
        }
        HashSet<Integer> vidSet = new HashSet<>(videoBackupPriorityList);
        for (VideoBackupConfig videoBackupConfig : allVideoBackupConfig) {
            if (!vidSet.contains(videoBackupConfig.id)) {
                throw new PriorityListMismatchException("视频配置id优先级列表与数据库中的不匹配！");
            }
        }
    }
    /**
     * 获取所有视频配置文件，并按照优先级排序
     */
    public List<VideoBackupConfig> selectAllVideoBackupConfigAndPrioritySort(){
        List<VideoBackupConfig> videoBackupConfigInfos = new LinkedList<>();
        //查询所有配置并按顺序
        for (Integer videoConfigId : settings.videoBackupPriorityList) {
            videoBackupConfigInfos.add(videoBackupConfigMapper.selectById(videoConfigId));
        }
        return videoBackupConfigInfos;
    }

    public VideoBackupConfig createNewVideoBackupConfig(String name) throws IOException {
        VideoBackupConfig videoBackupConfig = new VideoBackupConfig();
        videoBackupConfig.name = name;
        videoBackupConfig.video = VideoDownloadConfig.getDefault();
        videoBackupConfig.comment = CommentDownloadConfig.getDefault();
        videoBackupConfig.update = new VideoContentUpdateConfigList();
        videoBackupConfigMapper.insert(videoBackupConfig);
        settings.videoBackupPriorityList.add(videoBackupConfig.id);
        saveBackupSettings();
        return videoBackupConfig;
    }
    /**
     * 保存视频配置ID优先级顺序列表
     * @param ids 频配置ID优先级顺序
     * @throws IOException 保存到配置文件时出现异常
     */
    public void changeVideoBackupPriorityList(List<Integer> ids) throws PriorityListMismatchException, IOException {
        checkVideoBackupPriorityList(ids);
        settings.videoBackupPriorityList.clear();
        settings.videoBackupPriorityList.addAll(ids);
        saveBackupSettings();
    }

    public void deleteVideoBackupConfig(int id) throws IOException {
        videoBackupConfigMapper.updateVideoInfoConfigIdTo(VideoBackupConfigMapper.ID_FINAL,id);
        videoBackupConfigMapper.deleteById(id);
        settings.videoBackupPriorityList.removeIf(item -> item == id);
        saveBackupSettings();
    }

    private BackupSettings readBackupSettings() throws IOException {
        if (!backupSettingsFile.exists()){
            BackupSettings backupSettings = BackupSettings.getDefault();
            saveBackupSettings(backupSettings);
            return backupSettings;
        }
        return JSONConfig.readFromFile(backupSettingsFile,BackupSettings.class);
    }

    private void saveBackupSettings() throws IOException {
        saveBackupSettings(settings);
    }

    private void saveBackupSettings(BackupSettings settings) throws IOException {
        JSONConfig.writeToFile(backupSettingsFile,settings);
    }

    public void addUploaderBackup(BackupUploader backupUploader) throws IOException {
        UserContext userContext = userService.getUserContext(backupUploader.backupUserId);
        OkHttpClient httpClient = userContext.httpClient;
        FileService fileService = userContext.fileService;
        File avatarFile = fileService.newBCUploaderAvatarFile(MiscUtils.getEndPathForHttpUrl(backupUploader.avatarUrl));
        Request request = new Request.Builder()
                .url(backupUploader.avatarUrl)
                .build();
        ResponseBody body = OkHttpUtil.getResponseBodyNotNull(httpClient.newCall(request).execute());
        FileUtil.outputToFile(body.byteStream(),avatarFile);
        backupUploaderMapper.insert(backupUploader);
    }

    public void updateUploaderBackup(BackupUploader old, BackupUploader backupUploader) throws IOException {
        if (!old.avatarUrl.equals(backupUploader.avatarUrl)) {
            UserContext userContext = userService.getUserContext(backupUploader.backupUserId);
            OkHttpClient httpClient = userContext.httpClient;
            FileService fileService = userContext.fileService;
            File oldAvatarFile = fileService.newBCUploaderAvatarFile(MiscUtils.getEndPathForHttpUrl(old.avatarUrl));
            File avatarFile = fileService.newBCUploaderAvatarFile(MiscUtils.getEndPathForHttpUrl(backupUploader.avatarUrl));
            Request request = new Request.Builder()
                    .url(backupUploader.avatarUrl)
                    .build();
            ResponseBody body = OkHttpUtil.getResponseBodyNotNull(httpClient.newCall(request).execute());
            FileUtil.outputToFile(body.byteStream(), avatarFile);
            FileUtil.deleteOneFile(oldAvatarFile);
        }
        backupUploaderMapper.update(backupUploader);
    }

    public boolean jumpOverBackupSleep(){
        if (backupLoopThread == null){
            return false;
        }
        backupLoopThread.jumpOverSleep();
        return true;
    }

    public void checkNotRunning(){
        if (isRun()){
            throw new RuntimeConfigurationEditException("不能在备份器运行时进行配置修改相关操作，请先停止运行");
        }
    }

    public BackupSettings getSettings(){
        return settings;
    }


    public void updateBackupSettings(BackupSettings backupSettings) throws PriorityListMismatchException, IOException {
        checkNotRunning();
        checkVideoBackupPriorityList(backupSettings.videoBackupPriorityList);
        priorityManger = new PriorityManger(backupSettings.videoBackupPriorityList);
        settings = backupSettings;
        //同时更新userService的公共VIP用户
        userService.setGlobalVipUser(backupSettings.publicVipUserId);
        saveBackupSettings();
    }

    public synchronized Integer updateFavoriteVideoStates(long favId){
        Exchanger<Integer> exchanger = new Exchanger<>();
        BackupFav backupFav = backupFavMapper.selectByFavId(favId);
        UserContext userContext = userService.getUserContext(backupFav.backupUserId);
        return executeTask(exchanger,new RefreshFavVideoStatesThread(exchanger, userContext, backupFav));
    }

    public Integer updateUploaderVideoStates(long upUid) {
        Exchanger<Integer> exchanger = new Exchanger<>();
        BackupUploader backupUploader = backupUploaderMapper.selectByUpUid(upUid);
        UserContext userContext = userService.getUserContext(backupUploader.backupUserId);
        return executeTask(exchanger,new RefreshUpVideoStatesThread(exchanger,userContext,backupUploader));
    }

    public synchronized Integer deleteFavoriteBackupAndVideos(long favId) {
        Exchanger<Integer> exchanger = new Exchanger<>();
        return executeTask(exchanger,new FavoriteDeleteThread(exchanger, favId, archiveDeleteService));
    }

    public synchronized Integer deleteHistoryBackupAndVideos(long uid) {
        Exchanger<Integer> exchanger = new Exchanger<>();
        return executeTask(exchanger,new HistoryDeleteThread(exchanger, uid, archiveDeleteService));
    }

    public synchronized Integer deleteUploaderAndDeleteVideos(long upUid) {
        Exchanger<Integer> exchanger = new Exchanger<>();
        return executeTask(exchanger,new UploaderDeleteThread(exchanger, upUid, archiveDeleteService));
    }

    private synchronized Integer executeTask(Exchanger<Integer> exchanger, Thread thread) {
        if (operateThread != null && operateThread.isAlive()) {
            return null;
        }

        operateThread = thread;
        thread.start();
        try {
            return exchanger.exchange(null);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<VideoUpdatePlan> selectVideoPlansAscPage(int offset, int size,String bvid){
        return contentUpdatePlanMapper.selectVideoPlansAscPage(offset, size,bvid);
    }

    public synchronized int removeAllVideoUpdatePlans(){
        return contentUpdatePlanMapper.deleteAllVideoUpdatePlans();
    }

    public synchronized int removeVideoUpdatePlansByBvid(String bvid){
        int count = contentUpdatePlanMapper.deleteVideoUpdatePlansByBvid(bvid);
        if (updateThread != null) {
            updateThread.interrupt();
        }
        return count;
    }

    public synchronized int removeVideoUpdatePlansById(long planId){
        int count = contentUpdatePlanMapper.deleteVideoUpdatePlanById(planId);
        if (updateThread != null) {
            updateThread.interrupt();
        }
        return count;
    }

    public boolean checkVideoHasUpdatePlan(String bvid){
        return contentUpdatePlanMapper.checkVideoHasUpdatePlan(bvid);
    }

    public void pushVideoUpdateTask(UserContext userContext,VideoUpdatePlan plan){
        updateThread.pushTask(userContext, plan);
    }

    public void addVideoUpdateTasks(Long uid,ArchiveVideoInfo videoInfo,VideoDownloadConfig videoConfig, List<VideoContentUpdateConfig> updateConfigs){
        removeVideoUpdatePlansByBvid(videoInfo.bvid);
        updateThread.addVideoUpdatePlan(uid, videoInfo.bvid, videoInfo.avid, videoInfo.title, System.currentTimeMillis(),videoConfig,updateConfigs);
    }

    public static class PriorityListMismatchException extends Exception {
        public PriorityListMismatchException(String message) {
            super(message);
        }
    }



}
