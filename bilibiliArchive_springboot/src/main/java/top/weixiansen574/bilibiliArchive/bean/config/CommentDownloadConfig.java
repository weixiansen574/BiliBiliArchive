package top.weixiansen574.bilibiliArchive.bean.config;

import java.util.Objects;

public class CommentDownloadConfig {
    /**
     * 下载所有评论
     */
    public static final String MODE_ALL = "ALL";
    /**
     * 热门排序TOP，rootLimit设置根评论的限制数量，评论回复若嫌多可以使用usePreview，使用回复预览中的评论（小于等于3个）
     */
    public static final String MODE_HOT = "HOT";
    /**
     * 楼层嗅探，扫描楼层（仅根评论可扫楼）。如果用在更新上，将使用截止楼层（与增量更新的反向截止日期类似）。请注意，若要加到更新计划，正常下载与更新下载的评论配置都选这个模式
     * 若正常下载没选楼层，则更新要从头开始。若正常下载选了楼层，但是更新不选，将会导致新的评论无楼层
     * 前端隐藏此功能，开启方式：在控制台输入代码 `localStorage.setItem("enable_hidden_feature",true);`
     * ⚠️使用守则：禁止宣传隐藏功能，无论在什么地方，只允许会编程的你自己把玩此功能⚠️
     */
    public static final String MODE_FLOOR_SNIFFER = "FLOOR_SNIFFER";
    /**
     * 增量更新。仅用在更新上，使用最新排序下载，本地时间降序查询根评论，选第一个（发布时间最晚），以它的发布时间为锚点，直到指针指向的评论发布的时间戳<锚点的时间戳停止
     */
    public static final String MODE_LATEST_FIRST = "LATEST_FIRST";

    //下载全部
    public static final String REPLY_MODE_ALL = "ALL";
    //增量更新
    public static final String REPLY_MODE_INCREMENT = "INCREMENT";
    //带限制的从旧到新
    public static final String REPLY_MODE_COUNT_LIMIT = "COUNT_LIMIT";

    public String mode;
    public Integer rootLimit;//HOT模式下的根评论限制

    public String replyMode;//评论回复下载的方式，除了热门以外，其余配置只能（强制）为ALL
    public Integer replyLimit;//评论回复COUNT_LIMIT模式下的

    public static CommentDownloadConfig getDefault() {
        CommentDownloadConfig cfg = new CommentDownloadConfig();
        cfg.mode = MODE_ALL;
        cfg.rootLimit = 200;
        cfg.replyMode = REPLY_MODE_ALL;
        cfg.replyLimit = 0;
        return cfg;
    }

    public boolean check(){
        if(Objects.equals(mode, MODE_HOT)){
            if (rootLimit == null||rootLimit < 0){
                return false;
            }
            if (replyMode == null){
                return false;
            }
            if (Objects.equals(replyMode,REPLY_MODE_COUNT_LIMIT)){
                return replyLimit != null && replyLimit >= 0;
            }
        }
        return true;
    }



    //2024年11月 b站疑似将除了UP主之外的预览评论一律折叠，故删除此功能
    //public Boolean replyUsePreview;
}
