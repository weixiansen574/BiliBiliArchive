package top.weixiansen574.bilibiliArchive.bean.videoinfo;

import org.jetbrains.annotations.Nullable;
import top.weixiansen574.bilibiliArchive.bean.list.MetadataChangeList;
import top.weixiansen574.bilibiliArchive.bean.list.StaffList;
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
    public static final String STATE_SEARCH_BAN = "SEARCH_BAN";//禁止搜索。表现在搜索引擎点进视频，视频不见了，但是刷新网页后视频并没失效

    public static final String TRY_SEARCH_SUCCESS = "SUCCESS";
    public static final String TRY_SEARCH_NO_RESULT = "NO_RESULT";
    public static final String TRY_SEARCH_BAN_WORLD = "BAN_WORLD";//搜索禁词，例如“爱猫TV”

    public static final int DOWNLOAD_STATE_OK = 0;
    public static final int DOWNLOAD_STATE_DOWNLOADING = 1;
    public static final int DOWNLOAD_STATE_FAILED = 2;

    public static final int COMMENT_AREA_STATE_NORMAL = 0;
    public static final int COMMENT_AREA_STATE_CLOSED = 1;
    public static final int COMMENT_AREA_STATE_CHOSEN = 2;

    public String bvid;
    public long avid;
    public String title;
    public String desc;
    public int duration;
    public long ownerMid;
    public String ownerName;
    public String ownerAvatarUrl;
    public StaffList staff;
    public long view;
    public long danmaku;
    public long favorite;
    public long coin;
    public long like;
    public long share;
    public long reply;
    public String tname;
    public long ctime;
    public long pubdate;
    public String coverUrl;
    public VideoTagList tags;
    public VideoPageVersionList pagesVersionList;
    public long saveTime;
    public String state;
    public String trySearch;
    public int downloading;
    public long communityUpdateTime;
    public int configId;
    public int totalCommentFloor;
    //封面 标题 简介 Tags 这些信息的修改日志，视频信息所显示的保持第一版，视频被编辑后的信息将存到这里
    public MetadataChangeList metadataChanges;


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
        this.ownerAvatarUrl = videoInfo.owner.face;
        this.staff = videoInfo.staff != null ? new StaffList(videoInfo.staff) : null;
        this.view = videoInfo.stat.view;
        this.danmaku = videoInfo.stat.danmaku;
        this.favorite = videoInfo.stat.favorite;
        this.coin = videoInfo.stat.coin;
        this.like = videoInfo.stat.like;
        this.share = videoInfo.stat.share;
        this.reply = videoInfo.stat.reply;
        this.tname = videoInfo.tname;
        this.ctime = videoInfo.ctime;
        this.pubdate = videoInfo.pubdate;
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
        this.metadataChanges = new MetadataChangeList();
    }

    public boolean isFailed() {
        return !state.equals(STATE_NORMAL);
    }

    @Nullable
    public VideoPageVersion latestPageVersion(){
        if (pagesVersionList == null){
            return null;
        }
        if (pagesVersionList.size() == 0){
            return null;
        }
        return pagesVersionList.get(pagesVersionList.size() - 1);
    }

    public List<DownloadedVideoPage> latestPages(){
        VideoPageVersion latestPageVersion = latestPageVersion();
        if (latestPageVersion == null){
            return null;
        }
        return latestPageVersion.pages;
    }

    public boolean addToPageVersionsIfInconsistency(List<DownloadedVideoPage> pages){
        if (pagesVersionList == null){
            pagesVersionList = new VideoPageVersionList();
        }
        if (pagesVersionList.isEmpty()) {
            pagesVersionList.add(new VideoPageVersion(System.currentTimeMillis(), pages));
            return true;
        }
        VideoPageVersion lastVideoPageVersion = pagesVersionList.get(pagesVersionList.size() - 1);
        //数量不一致
        if (lastVideoPageVersion.pages.size() != pages.size()) {
            pagesVersionList.add(new VideoPageVersion(System.currentTimeMillis(), pages));
            return true;
        }
        //cid编号不一致
        boolean changed = false;
        for (int i = 0; i < lastVideoPageVersion.pages.size(); i++) {
            DownloadedVideoPage lPage = lastVideoPageVersion.pages.get(i);
            DownloadedVideoPage nPage = pages.get(i);
            if (lPage.page != nPage.page) {
                throw new IllegalArgumentException("两列表中的page顺序不一致！");
            }
            if (lPage.cid != nPage.cid) {
                pagesVersionList.add(new VideoPageVersion(System.currentTimeMillis(), pages));
                return true;
            }
            //如果只是画质更新了，则修改视频的画质，但存档时间留着不变
            if (lPage.codecId != nPage.codecId || lPage.qn != nPage.qn) {
                lPage.codecId = nPage.codecId;
                lPage.qn = nPage.qn;
                changed = true;
            }
        }
        return changed;
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
