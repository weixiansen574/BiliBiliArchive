package top.weixiansen574.bilibiliArchive.services;

import org.apache.ibatis.cursor.Cursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.weixiansen574.bilibiliArchive.bean.ArchiveComment;
import top.weixiansen574.bilibiliArchive.bean.backups.BackupUploader;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.*;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.BiliComment;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.SubtitleInfo;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.operation.progress.ProgressBar;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.mapper.comment.AvatarMapper;
import top.weixiansen574.bilibiliArchive.mapper.comment.CommentMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.*;
import top.weixiansen574.bilibiliArchive.util.FileUtil;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Service
public class ArchiveDeleteService {

    public final VideoInfoMapper videoInfoMapper;
    public final SubtitleMapper subtitleMapper;
    public final VideoFavoriteMapper videoFavoriteMapper;
    public final VideoHistoryMapper videoHistoryMapper;
    public final VideoUploaderMapper videoUploaderMapper;
    public final BackupFavMapper backupFavMapper;
    public final BackupHistoryMapper backupHistoryMapper;
    public final BackupUploaderMapper backupUploaderMapper;
    public final FileService fileService;
    public final CommentMapper commentMapper;
    public final AvatarMapper avatarMapper;

    @Autowired
    public ArchiveDeleteService
            (VideoInfoMapper videoInfoMapper, SubtitleMapper subtitleMapper, VideoHistoryMapper videoHistoryMapper,
             VideoUploaderMapper videoUploaderMapper, VideoFavoriteMapper videoFavoriteMapper,
             BackupFavMapper backupFavMapper, BackupHistoryMapper backupHistoryMapper, BackupUploaderMapper backupUploaderMapper,
             FileService fileService, CommentMapper commentMapper, AvatarMapper avatarMapper) {
        this.videoInfoMapper = videoInfoMapper;
        this.subtitleMapper = subtitleMapper;
        this.videoHistoryMapper = videoHistoryMapper;
        this.videoUploaderMapper = videoUploaderMapper;
        this.videoFavoriteMapper = videoFavoriteMapper;
        this.backupFavMapper = backupFavMapper;
        this.backupHistoryMapper = backupHistoryMapper;
        this.backupUploaderMapper = backupUploaderMapper;
        this.fileService = fileService;
        this.commentMapper = commentMapper;
        this.avatarMapper = avatarMapper;
    }

    @Transactional
    public List<HistoryVideoInfo> queryHistoryVideosOfDiskUsageOverLimit(long historyUid, long size){
        List<HistoryVideoInfo> videoInfos = new LinkedList<>();
        Cursor<HistoryVideoInfo> historyVideoInfos = videoHistoryMapper.selectAllNoFaiByUid(historyUid);
        long currentUsage = 0;
        for (HistoryVideoInfo videoInfo : historyVideoInfos) {
            long usage = fileService.calcVideoAllDiskUsage(videoInfo.bvid);
            currentUsage += usage;
            if (size < currentUsage){
                videoInfos.add(videoInfo);
            }
        }
        return videoInfos;
    }

    @Transactional
    public void deleteFavoriteAndVideos(long fid){
        int count = videoFavoriteMapper.selectVideoCount(fid);
        int index = 0;
        for (FavoriteVideoInfo videoInfo : videoFavoriteMapper.selectAllByFavId(fid)) {
            PG.contentAndData("删除收藏视频：%s「%s」……",new ProgressBar(count,index),videoInfo.bvid,videoInfo.title);
            videoFavoriteMapper.deleteByPrimaryKey(videoInfo.favId,videoInfo.bvid);
            deleteVideoArchiveIfNotRef(videoInfo.bvid);
            index++;
        }
        backupFavMapper.deleteById(fid);
    }

    @Transactional
    public void deleteHistoryAndVideos(long uid){
        int count = videoHistoryMapper.selectVideoCount(uid);
        int index = 0;
        for (HistoryVideoInfo videoInfo : videoHistoryMapper.selectAllByUid(uid)) {
            PG.contentAndData("删除历史记录视频：%s「%s」……",new ProgressBar(count,index),videoInfo.bvid,videoInfo.title);
            videoHistoryMapper.deleteByPrimaryKey(videoInfo.uid, videoInfo.bvid);
            deleteVideoArchiveIfNotRef(videoInfo.bvid);
            index++;
        }
        backupHistoryMapper.deleteByUid(uid);
    }

    @Transactional
    public boolean deleteHistoryVideo(long uid,String bvid){
        videoHistoryMapper.deleteByPrimaryKey(uid,bvid);
        return deleteVideoArchiveIfNotRef(bvid);
    }

    @Transactional
    public void deleteUploaderAndArchives(long upUid){
        int count = videoUploaderMapper.selectVideoCount(upUid);
        int index = 0;
        for (UploaderVideoInfo videoInfo : videoUploaderMapper.selectAllByUid(upUid)) {
            PG.contentAndData("删除UP主视频：%s「%s」……",new ProgressBar(count,index),videoInfo.bvid,videoInfo.title);
            videoUploaderMapper.deleteByPrimaryKey(videoInfo.uid,videoInfo.bvid);
            deleteVideoArchiveIfNotRef(videoInfo.bvid);
        }
        BackupUploader backupUploader = backupUploaderMapper.selectByUpUid(upUid);
        File avatarFile = fileService.newBCUploaderAvatarFile(MiscUtils.getEndPathForHttpUrl(backupUploader.avatarUrl));
        FileUtil.deleteOneIfExists(avatarFile);
        backupUploaderMapper.deleteByUpUid(upUid);
    }

    @Transactional
    public synchronized boolean deleteVideoArchiveIfNotRef(String bvid){
        //检查视频是否还有其他
        if (videoInfoMapper.checkVideoIsReferenced(bvid)) {
            return false;
        }
        ArchiveVideoInfo videoInfo = videoInfoMapper.selectByBvid(bvid);
        if (videoInfo == null){
            return true;
        }

        //删除评论
        System.out.printf("删除视频存档：%s「%s」\n",videoInfo.bvid,videoInfo.title);
        System.out.println("删除评论……");
        System.out.printf("已删除%d个评论\n",deleteComments(videoInfo.avid,CommentMapper.AREA_TYPE_VIDEO));
        //删除字幕
        for (VideoPageVersion videoPageVersion : videoInfo.pagesVersionList) {
            for (DownloadedVideoPage page : videoPageVersion.pages) {
                if (page.subtitles != null){
                    for (SubtitleInfo subtitle : page.subtitles) {
                        subtitleMapper.delete(subtitle.id);
                    }
                }
            }
        }

        //删除封面、视频、弹幕等文件
        System.out.println("删除封面、视频、弹幕等文件……");
        File file = fileService.newBvidDirFile(bvid);
        if (file.exists()){
            FileUtil.deleteDirs(file);
        }
        //如果只有本视频引用这个UP主头像，则删除
        String avatarUrl = videoInfo.ownerAvatarUrl;
        if (videoInfoMapper.selectUpAvatarCount(avatarUrl) == 1) {
            File avatarFile = fileService.newUploaderAvatarFile(MiscUtils.getEndPathForHttpUrl(avatarUrl));
            FileUtil.deleteOneIfExists(avatarFile);
            System.out.println("已删除UP主头像文件：" + avatarFile.getAbsolutePath());
        }
        videoInfoMapper.deleteByBvid(bvid);
        System.out.println("视频删除完成");
        return true;
    }

    @Transactional
    public int deleteComments(long oid,int type){
        //流式查询评论，不要用list，不然评论过分多会爆内存
        for (ArchiveComment comment : commentMapper.selectAllByArea(oid, type)) {
            //删除图片文件
            if (comment.pictures != null){
                for (BiliComment.Pictures picture : comment.pictures) {
                    File file = fileService.newCommentPictureFile(MiscUtils.getEndPathForHttpUrl(picture.img_src));
                    FileUtil.deleteOneIfExists(file);
                    //System.out.println("已删除评论图片文件：" + file.getAbsolutePath());
                }
            }
            //如果评论头像图片只有此评论在使用，则删除
            if (commentMapper.selectAvatarCount(comment.avatarUrl) <= 1){
                avatarMapper.delete(MiscUtils.getEndPathForHttpUrl(comment.avatarUrl));
            }
        }
        //直接直接根据评论区oid type删，效率高
        return commentMapper.deleteByArea(oid, type);
    }


}
