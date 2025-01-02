package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.weixiansen574.bilibiliArchive.bean.AddVideoUpdatePlansRequest;
import top.weixiansen574.bilibiliArchive.bean.VideoUpdateRequest;
import top.weixiansen574.bilibiliArchive.bean.contentupdate.VideoUpdatePlan;
import top.weixiansen574.bilibiliArchive.bean.response.BackupRCode;
import top.weixiansen574.bilibiliArchive.bean.response.BaseResponse;
import top.weixiansen574.bilibiliArchive.bean.response.ResponseCode;
import top.weixiansen574.bilibiliArchive.bean.response.data.FavBackupResp;
import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupFav;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupHistory;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupUploader;
import top.weixiansen574.bilibiliArchive.bean.response.data.VideoBackupConfigDeleteResp;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;
import top.weixiansen574.bilibiliArchive.core.BackupSettings;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.FavoriteVideo;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.operation.progress.ProgressObserver;
import top.weixiansen574.bilibiliArchive.core.util.FFmpegUtil;
import top.weixiansen574.bilibiliArchive.mapper.master.*;
import top.weixiansen574.bilibiliArchive.services.ArchiveDeleteService;
import top.weixiansen574.bilibiliArchive.services.BackupService;
import top.weixiansen574.bilibiliArchive.services.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/backup")
public class BackupController {
    //status

    //GET video-backup-config 获取所有视频备份配置，且按照优先级排序
    //POST video-backup-config 新建一个视频备份配置

    //GET PUT video-backup-config/{id} 获取或更新特定的视频备份配置

    //settings
    //settings/

    //user-fav/{uid}
    //fav/{fid}
    //history/{uid}
    //user-uploader/{uid}
    //uploader/{upUid}
    public UserService userService;
    public VideoBackupConfigMapper videoBackupConfigMapper;
    public BackupService backupService;
    public BackupFavMapper backupFavMapper;
    public VideoFavoriteMapper videoFavoriteMapper;
    public BackupHistoryMapper backupHistoryMapper;
    public VideoHistoryMapper videoHistoryMapper;
    public BackupUploaderMapper backupUploaderMapper;
    public VideoUploaderMapper videoUploaderMapper;
    public ArchiveDeleteService archiveDeleteService;

    @Autowired
    public BackupController
            (UserService userService, VideoBackupConfigMapper videoBackupConfigMapper, BackupService backupService, BackupFavMapper backupFavMapper,
             VideoFavoriteMapper videoFavoriteMapper, BackupHistoryMapper backupHistoryMapper,
             VideoHistoryMapper videoHistoryMapper, BackupUploaderMapper backupUploaderMapper,
             VideoUploaderMapper videoUploaderMapper, ArchiveDeleteService archiveDeleteService) {
        this.userService = userService;
        this.videoBackupConfigMapper = videoBackupConfigMapper;
        this.backupService = backupService;
        this.backupFavMapper = backupFavMapper;
        this.videoFavoriteMapper = videoFavoriteMapper;
        this.backupHistoryMapper = backupHistoryMapper;
        this.videoHistoryMapper = videoHistoryMapper;
        this.backupUploaderMapper = backupUploaderMapper;
        this.videoUploaderMapper = videoUploaderMapper;
        this.archiveDeleteService = archiveDeleteService;
    }

    @GetMapping("user-fav/{uid}")
    public BaseResponse<List<FavBackupResp>> getUserFavBackups(@PathVariable long uid) {
        List<BackupFav> backupFavList = backupFavMapper.selectByBackupUid(uid);
        List<FavBackupResp> list = new ArrayList<>(backupFavList.size());
        for (BackupFav backupFav : backupFavList) {
            list.add(new FavBackupResp(backupFav, videoFavoriteMapper.selectLatest(backupFav.favId),
                    videoFavoriteMapper.selectVideoCount(backupFav.favId)));
        }
        return BaseResponse.ok(list);
    }

    @GetMapping("fav/{fid}")
    public BaseResponse<FavBackupResp> getFavBackup(@PathVariable long fid) {
        BackupFav backupFav = backupFavMapper.selectByFavId(fid);
        return BaseResponse.ok(new FavBackupResp(backupFav, videoFavoriteMapper.selectLatest(backupFav.favId),
                videoFavoriteMapper.selectVideoCount(backupFav.favId)));
    }

    @DeleteMapping("fav/{favId}/{bvid}")
    public BaseResponse<Boolean> deleteFavVideo(@PathVariable long favId,@PathVariable String bvid) throws BiliBiliApiException, IOException {
        BackupFav backupFav = backupFavMapper.selectByFavId(favId);
        if (backupFav == null){
            return BaseResponse.error(BackupRCode.BACKUP_FAV_NOT_FOUND,"收藏夹备份未找到");
        }
        if (!videoFavoriteMapper.checkExists(favId,bvid)){
            return BaseResponse.notfound("收藏的视频未找到");
        }
        UserContext userContext = userService.getUserContext(backupFav.backupUserId);
        Set<String> videoIdSet = new HashSet<>();
        for (FavoriteVideo video : userContext.biliApiService.getFavoriteVideos(favId).success()) {
            videoIdSet.add(video.bvid);
        }
        if (videoIdSet.contains(bvid)){
            return BaseResponse.error(BackupRCode.FAV_VIDEO_ONLINE_NOT_REMOVED,"你在b站上的收藏夹还未删除此视频，无法删除本地存档");
        }
        videoFavoriteMapper.deleteByPrimaryKey(favId, bvid);
        return BaseResponse.ok(archiveDeleteService.deleteVideoArchiveIfNotRef(bvid));
    }

    @PostMapping("fav")
    public BaseResponse<FavBackupResp> addFavBackup(@RequestBody BackupFav backupFav) {
        backupService.checkNotRunning();
        if (!backupFav.check()) {
            return BaseResponse.error(BackupRCode.BACKUP_CONFIG_ILLEGAL, "非法配置");
        }

        if (backupFavMapper.checkExists(backupFav.favId)) {
            BackupFav exist = backupFavMapper.selectByFavId(backupFav.favId);
            return BaseResponse.error(BackupRCode.BACKUP_FAV_EXISTS, "收藏夹备份配置已存在，其备份者UID为：" + exist.backupUserId);
        }
        if (!userService.checkExists(backupFav.backupUserId)) {
            return BaseResponse.error(BackupRCode.USER_NOT_EXISTS, "备份用户不存在");
        }
        if (!videoBackupConfigMapper.checkExists(backupFav.videoBackupConfigId)) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NOT_EXISTS, "视频备份配置不存在，请检查videoBackupConfigId属性");
        }
        backupFavMapper.insert(backupFav);
        return BaseResponse.ok(new FavBackupResp(backupFav, null, 0));
    }

    @PutMapping("fav")
    public BaseResponse<FavBackupResp> updateFavBackup(@RequestBody BackupFav backupFav) {
        backupService.checkNotRunning();
        if (!backupFav.check()) {
            return BaseResponse.error(BackupRCode.BACKUP_CONFIG_ILLEGAL, "非法配置");
        }
        BackupFav old = backupFavMapper.selectByFavId(backupFav.favId);
        if (old == null) {
            return BaseResponse.error(BackupRCode.BACKUP_FAV_NOT_FOUND, "收藏夹备份配置不存在");
        }
        if (!userService.checkExists(backupFav.backupUserId)) {
            return BaseResponse.error(BackupRCode.USER_NOT_EXISTS, "备份用户不存在");
        }
        if (!videoBackupConfigMapper.checkExists(backupFav.videoBackupConfigId)) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NOT_EXISTS, "视频备份配置不存在，请检查videoBackupConfigId属性");
        }
        backupFavMapper.update(backupFav);
        return getFavBackup(backupFav.favId);

    }

    @GetMapping("update-fav-videos/{fid}")
    public SseEmitter updateFavVideoStatus(@PathVariable long fid) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        if (!backupFavMapper.checkExists(fid)) {
            emitter.send("NOT_FOUND");
        } else {
            Integer pid = backupService.updateFavoriteVideoStates(fid);
            if (pid != null) {
                emitter.send("SUCCESS");
                PG.addObserver(new ProgressObserver(emitter, pid));
                return emitter;
            } else {
                emitter.send("OTHER_TASKS_IN_PROGRESS");
            }
        }
        emitter.complete();
        return emitter;
    }

    @GetMapping("delete-fav/{fid}")
    public SseEmitter deleteFavBackup(@PathVariable long fid) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        if (backupService.isRun()) {
            emitter.send("BACKUP_IS_RUNNING");
        } else if (!backupFavMapper.checkExists(fid)) {
            emitter.send("NOT_FOUND");
        } else {
            Integer pid = backupService.deleteFavoriteBackupAndVideos(fid);
            if (pid != null) {
                emitter.send("SUCCESS");
                PG.addObserver(new ProgressObserver(emitter, pid));
                return emitter;
            } else {
                emitter.send("OTHER_TASKS_IN_PROGRESS");
            }
        }
        emitter.complete();
        return emitter;
    }

    @GetMapping("history/{uid}")
    public BaseResponse<BackupHistory> getUserHistoryBackup(@PathVariable long uid) {
        BackupHistory backupHistory = backupHistoryMapper.selectByUid(uid);
        if (backupHistory == null) {
            return BaseResponse.notfound("用户备份配置不存在");
        }
        return BaseResponse.ok(backupHistory);
    }

    @PostMapping("history")
    public BaseResponse<BackupHistory> addHistoryBackup(@RequestBody BackupHistory backupHistory) {
        return handleHistoryBackup(backupHistory, false);
    }

    @PutMapping("history")
    public BaseResponse<BackupHistory> updateHistoryBackup(@RequestBody BackupHistory backupHistory) {
        return handleHistoryBackup(backupHistory, true);
    }

    //删除历史记录视频（请确保你以后不会再看它，否则会再次存档）
    @DeleteMapping("history/{uid}/{bvid}")
    public BaseResponse<Boolean> deleteHistoryVideo(@PathVariable long uid,@PathVariable String bvid){
        if (!videoHistoryMapper.checkExists(uid,bvid)) {
            return BaseResponse.error(ResponseCode.NOT_FOUND,"历史记录视频未找到");
        }
        return BaseResponse.ok(archiveDeleteService.deleteHistoryVideo(uid,bvid),"历史记录视频删除成功");
    }

    private BaseResponse<BackupHistory> handleHistoryBackup(BackupHistory backupHistory, boolean isUpdate) {
        // 通用校验逻辑
        if (!backupHistory.check()) {
            return BaseResponse.error(BackupRCode.BACKUP_CONFIG_ILLEGAL, "非法配置");
        }
        if (!videoBackupConfigMapper.checkExists(backupHistory.videoBackupConfigId)) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NOT_EXISTS, "视频备份配置不存在，请检查videoBackupConfigId属性");
        }
        if (!userService.checkExists(backupHistory.uid)) {
            return BaseResponse.error(BackupRCode.USER_NOT_EXISTS, "用户不存在");
        }

        // 区分新增和更新的逻辑
        if (isUpdate) {
            BackupHistory old = backupHistoryMapper.selectByUid(backupHistory.uid);
            if (old == null) {
                return BaseResponse.error(BackupRCode.BACKUP_HISTORY_NOT_FOUND, "历史记录备份配置不存在");
            }
            backupHistoryMapper.update(backupHistory);
        } else {
            if (backupHistoryMapper.checkExists(backupHistory.uid)) {
                BackupHistory exist = backupHistoryMapper.selectByUid(backupHistory.uid);
                return new BaseResponse<>(BackupRCode.BACKUP_HISTORY_EXISTS, "历史记录备份已存在", exist);
            }
            backupHistoryMapper.insert(backupHistory);
        }
        return BaseResponse.ok(backupHistory);
    }

    @RequestMapping("delete-history/{uid}")
    public SseEmitter deleteHistoryBackup(@PathVariable long uid) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        if (backupService.isRun()) {
            emitter.send("BACKUP_IS_RUNNING");
            return emitter;
        } else if (!backupHistoryMapper.checkExists(uid)) {
            emitter.send("NOT_FOUND");
            return emitter;
        } else {
            Integer pid = backupService.deleteHistoryBackupAndVideos(uid);
            if (pid != null) {
                emitter.send("SUCCESS");
                PG.addObserver(new ProgressObserver(emitter, pid));
                return emitter;
            } else {
                emitter.send("OTHER_TASKS_IN_PROGRESS");
            }
        }
        emitter.complete();
        return emitter;
    }

    //DELETE history/{uid}/{bvid}
    //DELETE fav/{favId}/{bvid}

    //GET video-update-plans
    //GET video-update-plans/id/{id}
    //GET video-update-plans/bvid/{bvid}

    //DELETE video-update-plans/id/{id}
    //DELETE video-update-plans/bvid/{bvid}

    @GetMapping("user-uploader/{uid}")
    public BaseResponse<List<BackupUploader>> getUserUploaderBackups(@PathVariable long uid) {
        return BaseResponse.ok(backupUploaderMapper.selectByBackupUserId(uid));
    }

    @PostMapping("uploader")
    public BaseResponse<BackupUploader> addUploaderBackup(@RequestBody BackupUploader backupUploader) throws IOException {
        backupService.checkNotRunning();
        if (!backupUploader.check()) {
            return BaseResponse.error(BackupRCode.BACKUP_CONFIG_ILLEGAL, "非法配置");
        }
        if (backupUploaderMapper.checkExists(backupUploader.uid)) {
            BackupUploader exist = backupUploaderMapper.selectByUpUid(backupUploader.uid);
            return new BaseResponse<>(BackupRCode.BACKUP_UPLOADER_EXISTS, "UP主备份配置已存在，其备份者UID为：" +
                    exist.backupUserId, exist);
        }
        if (!videoBackupConfigMapper.checkExists(backupUploader.videoBackupConfigId)) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NOT_EXISTS, "视频备份配置不存在，请检查videoBackupConfigId属性");
        }
        if (!userService.checkExists(backupUploader.backupUserId)) {
            return BaseResponse.error(BackupRCode.USER_NOT_EXISTS, "备份用户不存在");
        }
        backupService.addUploaderBackup(backupUploader);
        return BaseResponse.ok(backupUploader);
    }

    @PutMapping("uploader")
    public BaseResponse<BackupUploader> updateUploaderBackup(@RequestBody BackupUploader backupUploader) throws IOException {
        backupService.checkNotRunning();
        if (!backupUploader.check()) {
            return BaseResponse.error(BackupRCode.BACKUP_CONFIG_ILLEGAL, "非法配置");
        }
        BackupUploader old = backupUploaderMapper.selectByUpUid(backupUploader.uid);
        if (old == null) {
            return BaseResponse.error(BackupRCode.BACKUP_UPLOADER_NOT_FOUND, "UP主备份配置不存在");
        }
        if (!videoBackupConfigMapper.checkExists(backupUploader.videoBackupConfigId)) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NOT_EXISTS, "视频备份配置不存在，请检查videoBackupConfigId属性");
        }
        if (!userService.checkExists(backupUploader.backupUserId)) {
            return BaseResponse.error(BackupRCode.USER_NOT_EXISTS, "备份用户不存在");
        }
        backupService.updateUploaderBackup(old, backupUploader);
        return getUploaderBackup(backupUploader.uid);
    }

    @GetMapping("uploader/{upUid}")
    public BaseResponse<BackupUploader> getUploaderBackup(@PathVariable long upUid) {
        return BaseResponse.ok(backupUploaderMapper.selectByUpUid(upUid));
    }

    @GetMapping("update-uploader-videos/{upUid}")
    public SseEmitter updateUploaderVideoStatus(@PathVariable long upUid) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        if (!backupUploaderMapper.checkExists(upUid)) {
            emitter.send("NOT_FOUND");
        } else {
            Integer pid = backupService.updateUploaderVideoStates(upUid);
            if (pid != null) {
                emitter.send("SUCCESS");
                PG.addObserver(new ProgressObserver(emitter, pid));
                return emitter;
            } else {
                emitter.send("OTHER_TASKS_IN_PROGRESS");
            }
        }
        emitter.complete();
        return emitter;
    }

    @RequestMapping("delete-uploader/{upUid}")
    public SseEmitter deleteUploaderBackup(@PathVariable long upUid) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        if (backupService.isRun()) {
            emitter.send("BACKUP_IS_RUNNING");
        } else if (!backupUploaderMapper.checkExists(upUid)) {
            emitter.send("NOT_FOUND");
        } else {
            Integer pid = backupService.deleteUploaderAndDeleteVideos(upUid);
            if (pid != null) {
                emitter.send("SUCCESS");
                PG.addObserver(new ProgressObserver(emitter, pid));
                return emitter;
            } else {
                emitter.send("OTHER_TASKS_IN_PROGRESS");
            }
        }
        emitter.complete();
        return emitter;
    }

    @GetMapping("video-backup-configs")
    public BaseResponse<List<VideoBackupConfig>> getAllVideoBackupConfigAndPrioritySort() {
        return BaseResponse.ok(backupService.selectAllVideoBackupConfigAndPrioritySort());
    }

    @PutMapping("video-backup-configs")
    public BaseResponse<Void> updateVideoBackupConfig(@RequestBody VideoBackupConfig videoBackupConfig) {
        backupService.checkNotRunning();
        if (!videoBackupConfigMapper.checkExists(videoBackupConfig.id)) {
            return BaseResponse.notfound("配置文件未找到");
        }
        if (videoBackupConfigMapper.checkNameExistsExceptId(videoBackupConfig.name, videoBackupConfig.id)) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NAME_EXISTS, "视频备份配置名称与其他配置冲突");
        }
        if (videoBackupConfig.name == null || videoBackupConfig.name.equals("")) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NEED_NAME, "配置需要有名字");
        }
        if (!videoBackupConfig.check()) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_ILLEGAL, "非法的视频备份配置");
        }
        videoBackupConfigMapper.update(videoBackupConfig);
        return BaseResponse.ok(null, "修改成功");
    }

    @DeleteMapping("video-backup-configs/{id}")
    public BaseResponse<VideoBackupConfigDeleteResp> deleteVideoBackupConfig(@PathVariable int id) throws IOException {
        backupService.checkNotRunning();
        if (!videoBackupConfigMapper.checkExists(id)) {
            return BaseResponse.notfound("配置文件未找到");
        }

        List<BackupFav> backupFavList = backupFavMapper.selectByVideoBackupConfigId(id);
        List<BackupHistory> backupHistoryList = backupHistoryMapper.selectByVideoBackupConfigId(id);
        List<BackupUploader> backupUploaderList = backupUploaderMapper.selectByVideoBackupConfigId(id);

        if (backupFavList.size() == 0 && backupHistoryList.size() == 0 && backupUploaderList.size() == 0) {
            backupService.deleteVideoBackupConfig(id);
            return BaseResponse.ok(null, "删除成功！");
        } else {
            return new BaseResponse<>(BackupRCode.VIDEO_BACKUP_CONFIG_REFERENCED,
                    "视频备份配置被引用，无法删除，请先将使用此配置的备份项改为其他备份配置",
                    new VideoBackupConfigDeleteResp(backupFavList, backupHistoryList, backupUploaderList));
        }
    }

    @PostMapping("video-backup-configs")
    public BaseResponse<VideoBackupConfig> createNewVideoBackupConfig(@RequestParam String name) throws IOException {
        backupService.checkNotRunning();
        if (name == null || name.equals("")) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NEED_NAME, "配置需要有名字");
        }
        if (videoBackupConfigMapper.checkExistsByName(name)) {
            return BaseResponse.error(BackupRCode.VIDEO_BACKUP_CONFIG_NAME_EXISTS, "视频备份配置名称已存在");
        }
        return BaseResponse.ok(backupService.createNewVideoBackupConfig(name));
    }

    @GetMapping("video-backup-configs/{id}")
    public BaseResponse<VideoBackupConfig> getVideoBackConfig(@PathVariable int id) {
        if (!videoBackupConfigMapper.checkExists(id)) {
            return BaseResponse.notfound("配置文件未找到");
        }
        return BaseResponse.ok(videoBackupConfigMapper.selectById(id));
    }

    @GetMapping("settings")
    public BaseResponse<BackupSettings> getBackupSettings() {
        return BaseResponse.ok(backupService.getSettings());
    }

    @PutMapping("settings")
    public BaseResponse<BackupSettings> updateBackupSettings(@RequestBody BackupSettings settings) throws IOException {
        backupService.checkNotRunning();
        if (!settings.check()) {
            return BaseResponse.error(BackupRCode.BACKUP_CONFIG_ILLEGAL, "非法的设置");
        }
        try {
            backupService.updateBackupSettings(settings);
            return getBackupSettings();
        } catch (BackupService.PriorityListMismatchException e) {
            return BaseResponse.error(BackupRCode.SETTING_PRIORITY_ID_NO_MISMATCH, e.getMessage());
        }
    }

    @PutMapping("settings/video-backup-priority-list")
    public BaseResponse<Void> changeVideoBackupConfigPriorityCfgList(@RequestBody List<Integer> ids) throws IOException {
        backupService.checkNotRunning();
        try {
            backupService.changeVideoBackupPriorityList(ids);
            return BaseResponse.ok(null);
        } catch (BackupService.PriorityListMismatchException e) {
            return BaseResponse.error(BackupRCode.SETTING_PRIORITY_ID_NO_MISMATCH, e.getMessage());
        }
    }

    @GetMapping("state/start")
    public BaseResponse<Long> start() {
        if (!FFmpegUtil.checkFFmpegInstalled()) {
            return BaseResponse.error(BackupRCode.FFMPEG_NOT_INSTALLED, "启动失败，ffmpeg未安装，请在服务安装ffmpeg");
        }
        backupService.start();
        return BaseResponse.ok(backupService.getRunAt(), "启动成功");
    }

    @GetMapping("state/stop")
    public BaseResponse<Void> stop() {
        backupService.stop();
        return BaseResponse.ok(null, "停止成功");
    }

    @GetMapping("state/run-at")
    public BaseResponse<Long> getRunAt() {
        return BaseResponse.ok(backupService.getRunAt());
    }

    @GetMapping("state/jump-over-backup-sleep")
    public BaseResponse<Boolean> jumpOverBackupSleep() {
        return BaseResponse.ok(backupService.jumpOverBackupSleep());
    }

    @GetMapping({"video-update-plans", "video-update-plans/bvid/{bvid}"})
    public BaseResponse<List<VideoUpdatePlan>> getVideoUpdatePlans(
            @PathVariable(required = false) String bvid,
            @RequestParam(defaultValue = "1") int pn,
            @RequestParam(defaultValue = "30") int ps) {
        pn = Math.max(pn, 1);
        int offset = (pn - 1) * ps; // 计算偏移量
        return BaseResponse.ok(backupService.selectVideoPlansAscPage(offset, ps, bvid));
    }

    @DeleteMapping("video-update-plans/id/{id}")
    public BaseResponse<Integer> deleteVideoUpdateById(@PathVariable long id){
        return BaseResponse.ok(backupService.removeVideoUpdatePlansById(id));
    }

    @DeleteMapping("video-update-plans/bvid/{bvid}")
    public BaseResponse<Integer> deleteVideoUpdateByBvid(@PathVariable String bvid){
        return BaseResponse.ok(backupService.removeVideoUpdatePlansByBvid(bvid));
    }

    //POST update-video/{bvid}
    //POST add-video-update-plans/{bvid}


    //TIPS 立刻更新视频，需要更新计划列表里不存在此视频的计划。执行更新，视频备份配置优先级将调到FINAL，不会被其他备份配置覆盖
    @PostMapping("update-video")
    public BaseResponse<Boolean> updateVideoContent(@RequestBody VideoUpdateRequest request){
        if (!request.check()){
            return BaseResponse.illegalRequest("非法的视频更新信息");
        }
        UserContext userContext = userService.getUserContext(request.uid);
        if (userContext == null){
            return BaseResponse.error(BackupRCode.USER_NOT_EXISTS,"用户不存在");
        }
        ArchiveVideoInfo videoInfo = userContext.videoInfoMapper.selectByBvid(request.bvid);
        if (videoInfo == null){
            return BaseResponse.error(BackupRCode.VIDEO_NOT_EXISTS,"要更新的视频不存在");
        }
        if (backupService.checkVideoHasUpdatePlan(videoInfo.bvid)){
            return BaseResponse.error(BackupRCode.UPDATE_PLAN_CONFLICTS,"视频与现有更新计划冲突，请先移除该视频所有更新计划");
        }
        //需要运行时才可
        if (!backupService.isRun()){
            return BaseResponse.ok(false);
        }
        //修改视频备份配置ID为FINAL，使其不能被覆盖
        userContext.videoInfoMapper.updateConfigId(request.bvid, 0);
        VideoUpdatePlan plan = new VideoUpdatePlan();
        plan.bvid = videoInfo.bvid;
        plan.avid = videoInfo.avid;
        plan.title = videoInfo.title;
        plan.videoCodecId = request.video.codecId;
        plan.videoQuality = request.video.clarity;
        plan.updateVideoAndDanmaku = request.updateVideoAndDanmaku;
        plan.commentConfig1 = request.commentConfig;
        backupService.pushVideoUpdateTask(userContext,plan);
        return BaseResponse.ok(true);
    }

    //TIPS 向视频更新计划列表插入视频备份配置，起始时间为当前时间。此操作将移除已存在的视频更新计划，且视频备份配置优先级将调到FINAL，不会被其他备份配置覆盖
    @PostMapping("add-video-update-plans")
    public BaseResponse<Boolean> addVideoUpdatePlans(@RequestBody AddVideoUpdatePlansRequest request){
        if (!request.check()){
            return BaseResponse.illegalRequest("非法的视频更新计划信息");
        }
        UserContext userContext = userService.getUserContext(request.uid);
        if (userContext == null){
            return BaseResponse.error(BackupRCode.USER_NOT_EXISTS,"用户不存在");
        }
        ArchiveVideoInfo videoInfo = userContext.videoInfoMapper.selectByBvid(request.bvid);
        if (videoInfo == null){
            return BaseResponse.error(BackupRCode.VIDEO_NOT_EXISTS,"要更新的视频不存在");
        }
        //需要运行时才可
        if (!backupService.isRun()){
            return BaseResponse.ok(false);
        }
        userContext.videoInfoMapper.updateConfigId(request.bvid, 0);
        backupService.addVideoUpdateTasks(request.uid, videoInfo,request.video,request.update);
        return BaseResponse.ok(true);
    }





}