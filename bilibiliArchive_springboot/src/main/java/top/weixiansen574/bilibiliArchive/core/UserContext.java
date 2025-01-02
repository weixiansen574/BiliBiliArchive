package top.weixiansen574.bilibiliArchive.core;

import okhttp3.OkHttpClient;
import top.weixiansen574.bilibiliArchive.bean.BiliUser;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.downloaders.VideoDownloader;
import top.weixiansen574.bilibiliArchive.mapper.comment.AvatarMapper;
import top.weixiansen574.bilibiliArchive.mapper.comment.CommentMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.*;
import top.weixiansen574.bilibiliArchive.services.ArchiveDeleteService;
import top.weixiansen574.bilibiliArchive.services.FileService;

public final class UserContext {
    public final BiliUser biliUser;
    public final OkHttpClient httpClient;
    public final BiliApiService biliApiService;
    public final FileService fileService;
    public final VideoInfoMapper videoInfoMapper;
    public final VideoFavoriteMapper videoFavoriteMapper;
    public final VideoHistoryMapper videoHistoryMapper;
    public final VideoUploaderMapper videoUploaderMapper;
    public final CommentMapper commentMapper;
    public final AvatarMapper avatarMapper;
    public final SubtitleMapper subtitleMapper;
    public final VideoDownloader videoDownloader;
    public final ArchiveDeleteService archiveDeleteService;

    private UserContext publicVipUser;


    public UserContext(BiliUser biliUser, OkHttpClient httpClient, BiliApiService biliApiService,
                       FileService fileService, VideoInfoMapper videoInfoMapper, VideoFavoriteMapper videoFavoriteMapper,
                       VideoHistoryMapper videoHistoryMapper, VideoUploaderMapper videoUploaderMapper,
                       CommentMapper commentMapper, AvatarMapper avatarMapper, SubtitleMapper subtitleMapper, ArchiveDeleteService archiveDeleteService) {
        this.biliUser = biliUser;
        this.httpClient = httpClient;
        this.biliApiService = biliApiService;
        this.fileService = fileService;
        this.videoInfoMapper = videoInfoMapper;
        this.videoFavoriteMapper = videoFavoriteMapper;
        this.videoHistoryMapper = videoHistoryMapper;
        this.videoUploaderMapper = videoUploaderMapper;
        this.commentMapper = commentMapper;
        this.avatarMapper = avatarMapper;
        this.subtitleMapper = subtitleMapper;
        this.archiveDeleteService = archiveDeleteService;
        this.videoDownloader = new VideoDownloader(httpClient,biliApiService,fileService,commentMapper,avatarMapper,subtitleMapper);
    }

    public UserContext getPublicVipUserContext() {
        return publicVipUser;
    }

    public void setPublicVipUserContext(UserContext publicVipUser) {
        this.publicVipUser = publicVipUser;
    }

    @Override
    public String toString() {
        return "UserContext[" +
                "biliUser=" + biliUser + ", " +
                "httpClient=" + httpClient + ", " +
                "biliApiService=" + biliApiService + ", " +
                "fileService=" + fileService + ", " +
                "commentMapper=" + commentMapper + ", " +
                "avatarMapper=" + avatarMapper + ", " +
                "subtitleMapper=" + subtitleMapper + ']';
    }

}
