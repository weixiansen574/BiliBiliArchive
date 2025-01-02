package top.weixiansen574.bilibiliArchive.bean.videoinfo;

import top.weixiansen574.bilibiliArchive.bean.list.VideoPageVersionList;
import top.weixiansen574.bilibiliArchive.bean.list.VideoTagList;
import top.weixiansen574.bilibiliArchive.core.biliApis.GeneralResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;

import java.util.List;

public class ArchiveVideoInfo {

    /*public static final int STATE_NORMAL = 0;
    public static final int STATE_FAILED = 404;
    public static final int STATE_FAILED_UP_DELETE = 4049;//对应收藏夹里“UP主删除”的attr值：9
    public static final int STATE_BACKUP_IS_NOT_SUPPORTED = -403;//不支持备份的视频，比如充电专属视频
    public static final int STATE_FAILED_AND_NO_BACKUP = -404;*/

    public static final String STATE_NORMAL = "NORMAL";
    public static final String STATE_FAILED = "FAILED";
    public static final String STATE_FAILED_UP_DELETE = "FAILED_UP_DELETE";
    public static final String STATE_BACKUP_IS_NOT_SUPPORTED = "BACKUP_IS_NOT_SUPPORTED";
    public static final String STATE_FAILED_AND_NO_BACKUP = "FAILED_AND_NO_BACKUP";
    public static final String STATE_PRIVATE = "PRIVATE";//作者设置仅自己可见

    public static final String STATE_SHADOW_BAN = "SHADOW_BAN";//虽然我不是哔哩发评反诈，但是它视频也会搞诈骗啊（欺骗UP主）。当然你不是视频发布者也有办法获知

    public static final int DOWNLOAD_STATE_OK = 0;
    public static final int DOWNLOAD_STATE_DOWNLOADING = 1;
    public static final int DOWNLOAD_STATE_FAILED = 2;

    public static final int COMMENT_AREA_STATE_CLOSED = 0;
    public static final int COMMENT_AREA_STATE_NORMAL = 1;
    public static final int COMMENT_AREA_STATE_CHOSEN = 2;

    public String bvid;
    public long avid;
    public String title;
    public String desc;
    public int duration;
    public long ownerMid;
    public String ownerName;
    public long view;
    public long danmaku;
    public long favorite;
    public long coin;
    public long like;
    public long share;
    public long reply;
    public String tname;
    public long ctime;
    public String coverUrl;
    public VideoTagList tags;
    public VideoPageVersionList pagesVersionList;
    public long saveTime;
    public String state;
    public int downloading;
    public long communityUpdateTime;
    public int configId;
    public String ownerAvatarUrl;
    public int totalCommentFloor;


    public ArchiveVideoInfo() {
    }

    private ArchiveVideoInfo(VideoInfo videoInfo, VideoPageVersionList pagesVersionList) {
        this.bvid = videoInfo.bvid;
        this.avid = videoInfo.aid;
        this.title = videoInfo.title;
        this.desc = videoInfo.desc;
        this.duration = videoInfo.duration;
        this.ownerMid = videoInfo.owner.mid;
        this.ownerName = videoInfo.owner.name;
        this.view = videoInfo.stat.view;
        this.danmaku = videoInfo.stat.danmaku;
        this.favorite = videoInfo.stat.favorite;
        this.coin = videoInfo.stat.coin;
        this.like = videoInfo.stat.like;
        this.share = videoInfo.stat.share;
        this.reply = videoInfo.stat.reply;
        this.tname = videoInfo.tname;
        this.ctime = videoInfo.ctime;
        this.coverUrl = videoInfo.pic;
        this.pagesVersionList = pagesVersionList;
    }

    public ArchiveVideoInfo(VideoInfo videoInfo, VideoPageVersionList pageVersions, String state, long saveTime,
                            int configId, long communityUpdateTime, int downloading) {
        this(videoInfo, pageVersions);
        this.saveTime = saveTime;
        this.state = state;
        this.configId = configId;
        this.downloading = downloading;
        this.communityUpdateTime = communityUpdateTime;
        this.ownerAvatarUrl = videoInfo.owner.face;
    }


    public boolean isFailed() {
        return !state.equals(STATE_NORMAL);
    }

    public static void addToLastIfInconsistency(List<VideoPageVersion> pageVersions, List<DownloadedVideoPage> pages) {
        //直接为空
        if (pageVersions.isEmpty()) {
            pageVersions.add(new VideoPageVersion(System.currentTimeMillis(), pages));
            return;
        }

        VideoPageVersion lastVideoPageVersion = pageVersions.get(pageVersions.size() - 1);

        //数量不一致
        if (lastVideoPageVersion.pages.size() != pages.size()) {
            pageVersions.add(new VideoPageVersion(System.currentTimeMillis(), pages));
            return;
        }
        //cid编号不一致
        for (int i = 0; i < lastVideoPageVersion.pages.size(); i++) {
            DownloadedVideoPage lPage = lastVideoPageVersion.pages.get(i);
            DownloadedVideoPage nPage = pages.get(i);
            if (lPage.page != nPage.page) {
                throw new IllegalArgumentException("两列表中的page顺序不一致！");
            }
            if (lPage.cid != nPage.cid) {
                pageVersions.add(new VideoPageVersion(System.currentTimeMillis(), pages));
                return;
            }
            //如果只是画质更新了，则修改视频的画质，但存档时间留着不变
            if (lPage.codecId != nPage.codecId || lPage.qn != nPage.qn) {
                lPage.codecId = nPage.codecId;
                lPage.qn = nPage.qn;
            }
        }
    }

    public static String getStateFromApiResponseCode(int code) {
        return switch (code) {
            case 0 -> STATE_NORMAL;
            case GeneralResponse.ErrorCodes.VideoInfo.FAILED -> STATE_FAILED;
            case GeneralResponse.ErrorCodes.VideoInfo.UP_DELETED -> STATE_FAILED_UP_DELETE;
            case GeneralResponse.ErrorCodes.VideoInfo.PRIVATE -> STATE_PRIVATE;
            default -> null;
        };
    }

}
