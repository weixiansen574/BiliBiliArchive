package top.weixiansen574.bilibiliArchive.core;

import java.util.List;

public class PriorityManger {
    public static final int CONFIG_ID_FINAL = 0;
    private final List<Integer> videoBackupPriorityList;

    public PriorityManger(List<Integer> videoBackupPriorityList) {
        this.videoBackupPriorityList = videoBackupPriorityList;
    }

    /**
     * 获取某视频备份配置的优先级
     * @param configId 视频备份配置的ID
     * @return 优先级，越低越高
     */
    public int getVideoCfgPriority(int configId) {
        if (configId == CONFIG_ID_FINAL){
            return 0;//FINAL优先级最大
        }
        for (int i = 0; i < videoBackupPriorityList.size(); i++) {
            if (configId == videoBackupPriorityList.get(i)) {
                return i + 1;//因为FINAL配置要占一位，所以index+1
            }
        }
        throw new RuntimeException("configId:" + configId + " 没有对应的");
    }
}
