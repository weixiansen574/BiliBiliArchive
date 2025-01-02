package top.weixiansen574.bilibiliArchive.bean.backups;

import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;
import top.weixiansen574.bilibiliArchive.bean.list.BlackListUserList;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

public class BackupHistory {
    public static final String DELETE_METHOD_DISABLE = "DISABLE";//禁用删除
    public static final String DELETE_METHOD_ITEM_QUANTITY = "ITEM_QUANTITY";//条目
    public static final String DELETE_METHOD_DAYS = "DAYS";//view_at 时间戳间隔天数
    public static final String DELETE_METHOD_DISK_USAGE = "DISK_USAGE";//占用空间

    public static final String B = "B";
    public static final String KB = "KB";
    public static final String MB = "MB";
    public static final String GB = "GB";
    public static final String TB = "TB";


    public Long uid;
    public String autoDeleteMethod;
    public Integer deleteByDays;
    public Double deleteByDiskUsage;
    public String deleteByDiskUsageUnit;
    public Integer deleteByItemQuantity;
    public Integer releaseTimeLimitDay;
    public BlackListUserList uploaderBlackList;
    public Boolean videoBackupEnable;
    public Integer videoBackupConfigId;

    public VideoBackupConfig videoBackupConfig;

    public BackupHistory() {}

    public BackupHistory(Long uid, String autoDeleteMethod, Integer deleteByDays, Double deleteByDiskUsage,
                         Integer deleteByItemQuantity, Integer releaseTimeLimitDay, BlackListUserList uploaderBlackList,
                         Boolean videoBackupEnable, Integer videoBackupConfigId) {
        this.uid = uid;
        this.autoDeleteMethod = autoDeleteMethod;
        this.deleteByDays = deleteByDays;
        this.deleteByDiskUsage = deleteByDiskUsage;
        this.deleteByItemQuantity = deleteByItemQuantity;
        this.releaseTimeLimitDay = releaseTimeLimitDay;
        this.uploaderBlackList = uploaderBlackList;
        this.videoBackupEnable = videoBackupEnable;
        this.videoBackupConfigId = videoBackupConfigId;
    }

    public boolean check(){
        if (!MiscUtils.notNulls(uid,autoDeleteMethod,videoBackupEnable,videoBackupConfigId)) {
            return false;
        }
        return switch (autoDeleteMethod) {
            case DELETE_METHOD_ITEM_QUANTITY -> deleteByItemQuantity != null;
            case DELETE_METHOD_DAYS -> deleteByDays != null;
            case DELETE_METHOD_DISK_USAGE -> checkDiskUsageCfg();
            default -> autoDeleteMethod.equals(DELETE_METHOD_DISABLE);
        };
    }

    private boolean checkDiskUsageCfg(){
        if (deleteByDiskUsage == null || deleteByDiskUsageUnit == null) {
            return false;
        }
        return MiscUtils.matchOne(deleteByDiskUsageUnit, B, KB, MB, GB, TB);
    }

    public long calcDeleteByDiskUsageByte() {
        return switch (deleteByDiskUsageUnit) {
            case B -> Math.round(deleteByDiskUsage);
            case KB -> Math.round(deleteByDiskUsage * 1024);
            case MB -> Math.round(deleteByDiskUsage * 1024 * 1024);
            case GB -> Math.round(deleteByDiskUsage * 1024 * 1024 * 1024);
            case TB -> Math.round(deleteByDiskUsage * 1024 * 1024 * 1024 * 1024);
            default -> -1L; // 返回无效值
        };
    }





}
