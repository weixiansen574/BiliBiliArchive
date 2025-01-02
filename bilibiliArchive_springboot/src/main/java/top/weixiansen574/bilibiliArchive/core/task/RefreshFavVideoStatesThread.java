package top.weixiansen574.bilibiliArchive.core.task;

import top.weixiansen574.bilibiliArchive.bean.backups.BackupFav;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.FavoriteVideoInfo;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.FavoriteVideo;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.FavoriteVideoItem;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.DetailedFavoriteVideoInfosPage;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.operation.progress.ProgressBar;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoFavoriteMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoInfoMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Exchanger;

public class RefreshFavVideoStatesThread extends Thread {
    private final Exchanger<Integer> idChanger;
    private final BackupFav backupFav;
    private final long favId;

    private final BiliApiService biliApiService;
    private final VideoFavoriteMapper videoFavoriteMapper;
    private final VideoInfoMapper videoInfoMapper;


    public RefreshFavVideoStatesThread(Exchanger<Integer> idChanger, UserContext userContext, BackupFav backupFav) {
        this.idChanger = idChanger;
        this.backupFav = backupFav;
        this.favId = backupFav.favId;

        this.biliApiService = userContext.biliApiService;
        this.videoFavoriteMapper = userContext.videoFavoriteMapper;
        this.videoInfoMapper = userContext.videoInfoMapper;
    }

    @Override
    public void run() {
        try {
            idChanger.exchange(PG.titleInit(String.format("更新收藏夹视频状态：「%s」（所属：%s）",
                    backupFav.favName, backupFav.ownerName)));
        } catch (InterruptedException e) {
            PG.remove();
            return;
        }
        PG.content("正在查询视频列表");
        List<FavoriteVideoInfo> favoriteVideoInfos = videoFavoriteMapper.selectAllByFavId(favId);
        for (int i = 0; i < favoriteVideoInfos.size(); i++) {
            FavoriteVideoInfo videoInfo = favoriteVideoInfos.get(i);
            String oldState = videoInfo.state;
            try {
                //获取视频新状态
                MiscUtils.getVideoInfoOrChangeState(biliApiService,videoInfo);
                PG.contentAndData("%s『%s』状态：%s  ==> %s", new ProgressBar(favoriteVideoInfos.size(), i + 1),
                        videoInfo.bvid, MiscUtils.omit(videoInfo.title, 30), oldState, videoInfo.state);
                videoInfoMapper.updateVideoStatus(videoInfo.state, videoInfo.bvid);
                MiscUtils.sleepNoException(100);//限速，避免操作太快API被封，真不是负优化哈
            } catch (IOException e) {
                PG.content("获取视频状态失败，发生IO异常：%s", e.getMessage());
                MiscUtils.sleepNoException(5000);
            } catch (BiliBiliApiException e) {
                PG.content(e.getMessage());
                MiscUtils.sleepNoException(10000);//不让异常信息一闪而过
            }
        }

        PG.content("正在查找在收藏夹里被一同屏蔽的视频……");
        HashSet<String> trustedIdSet = new HashSet<>(favoriteVideoInfos.size());
        HashSet<String> unreliableIdSet = new HashSet<>(favoriteVideoInfos.size());
        try {
            List<FavoriteVideo> favoriteVideoList = biliApiService.getFavoriteVideos(favId).success();
            for (FavoriteVideo favoriteVideo : favoriteVideoList) {
                trustedIdSet.add(favoriteVideo.bvid);
            }
            for (int pn = 1; true; pn++) {
                DetailedFavoriteVideoInfosPage page = biliApiService
                        .getFavoriteVideosInDetailed(backupFav.favId, pn, 20, 0)
                        .success("获取收藏夹视频失败");
                if (page.medias == null || page.medias.size() == 0) {
                    break;
                }
                for (FavoriteVideoItem media : page.medias) {
                    unreliableIdSet.add(media.bvid);
                }
            }
            for (String bvid : trustedIdSet) {
                videoFavoriteMapper.updateIsFavBan(!unreliableIdSet.contains(bvid),favId,bvid);
            }
        } catch (IOException | BiliBiliApiException e) {
            PG.content("获取收藏夹视频列表时发生异常：%s", e);
            MiscUtils.sleepNoException(10000);
        }
        PG.remove();
    }


}
