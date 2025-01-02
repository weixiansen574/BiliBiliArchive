package top.weixiansen574.bilibiliArchive.bean.config;

import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

public class VideoContentUpdateConfig {
    public Integer loopCount;
    public Long interval;//单位：分钟
    public Boolean updateVideoAndDanmaku;
    public CommentDownloadConfig comment1;//为null不更新评论
    public CommentDownloadConfig comment2;//二次更新评论，比如第一次增量，第二次热门前几更新点赞与回复
    public CommentDownloadConfig comment3;//三次更新评论，如果此配置生效，在此节点，二次更新评论将1不执行
    public int comment2Spacing;
    public int comment3Spacing;

    public long intervalToMs(){
        return interval * 60000;
    }

    //对于关注UP主的特色优化，如果都按增量模式下载，那么评论的点赞无法更新，此时我们再进行一次限制性的爬取热门前几的评论，就可以更新部分重要评论的点赞以及回复列表
    //十万火急之双刷模式，面临场景：米哈游被冲，疯狂删评、键政冲塔，评论区很精彩，但评论可能很快被删
    //专治秋后算账！依据哔哩发评反诈，可回复评论试探别人的评论是否为 shadow ban
    //以下是评论，间距2
    //Main          ■■■■■■■■■■■■■■■
    //Secondary     □□■□□■□□■□□■□□■

    public boolean check(){
        if (!MiscUtils.notNulls(loopCount,interval,updateVideoAndDanmaku)) {
            return false;
        }
        if (comment1 != null && !comment1.check()){
            return false;
        }
        if (comment2 != null && !comment2.check()){
            return false;
        }
        if (comment3 != null && !comment3.check()){
            return false;
        }
        return interval > 0;
    }


}
