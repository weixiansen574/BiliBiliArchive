package top.weixiansen574.bilibiliArchive.core;

import top.weixiansen574.bilibiliArchive.bean.BlackListUser;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class BackupSettings {
    public Integer taskPoolSize;
    public Integer intervalMinuteOfLoop;
    public Long publicVipUserId;
    public List<Integer> videoBackupPriorityList;
    public List<BlackListUser> globalUpBlackList;


    public static BackupSettings getDefault(){
        BackupSettings settings = new BackupSettings();
        settings.taskPoolSize = 1;
        settings.intervalMinuteOfLoop = 60;
        settings.publicVipUserId = null;
        settings.videoBackupPriorityList = new ArrayList<>();
        settings.globalUpBlackList = new ArrayList<>();
        return settings;
    }

    public boolean check(){
        if (!MiscUtils.notNulls(taskPoolSize, intervalMinuteOfLoop,videoBackupPriorityList)) {
            return false;
        }
        return intervalMinuteOfLoop >= 1 && taskPoolSize >= 1;
    }
}
