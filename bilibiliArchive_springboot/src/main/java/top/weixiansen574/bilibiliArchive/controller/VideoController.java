package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.weixiansen574.bilibiliArchive.bean.response.data.VideoInfoResp;
import top.weixiansen574.bilibiliArchive.bean.response.BaseResponse;
import top.weixiansen574.bilibiliArchive.bean.response.ResponseCode;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.*;
import top.weixiansen574.bilibiliArchive.mapper.comment.CommentMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoFavoriteMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoHistoryMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoInfoMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoUploaderMapper;
import top.weixiansen574.bilibiliArchive.services.ArchiveDeleteService;
import top.weixiansen574.bilibiliArchive.services.FileService;
import top.weixiansen574.bilibiliArchive.util.FileUtil;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {
    //info/{bvid}
    //favorite/{favId}
    //history/{uid}
    //uploader/{upUid}
    public static final int SORT_BY_SAVE_TIME = 1;
    public static final int SORT_BY_UPDATE_TIME = 2;
    public static final int SORT_BY_UPLOAD_TIME = 3;

    public static final int SORT_TYPE_DESC = 0;
    public static final int SORT_TYPE_ASC = 1;


    public final VideoInfoMapper videoInfoMapper;
    public final VideoFavoriteMapper videoFavoriteMapper;
    public final VideoHistoryMapper videoHistoryMapper;
    public final VideoUploaderMapper videoUploaderMapper;
    public final CommentMapper commentMapper;
    public final FileService fileService;

    @Autowired
    public VideoController(VideoInfoMapper videoInfoMapper, VideoFavoriteMapper videoFavoriteMapper,
                           VideoHistoryMapper videoHistoryMapper, VideoUploaderMapper videoUploaderMapper,
                           CommentMapper commentMapper, ArchiveDeleteService archiveDeleteService, FileService fileService) {
        this.videoInfoMapper = videoInfoMapper;
        this.videoFavoriteMapper = videoFavoriteMapper;
        this.videoHistoryMapper = videoHistoryMapper;
        this.videoUploaderMapper = videoUploaderMapper;
        this.commentMapper = commentMapper;
        this.fileService = fileService;
    }

    @GetMapping("info/{bvid}")
    public BaseResponse<ArchiveVideoInfo> getVideoInfo(@PathVariable String bvid) {
        ArchiveVideoInfo videoInfo = videoInfoMapper.selectByBvid(bvid);
        if (videoInfo == null) {
            return BaseResponse.error(ResponseCode.NOT_FOUND, "视频信息未找到");
        }
        return BaseResponse.ok(videoInfo);
    }

    @GetMapping("info-ext/{bvid}")
    public BaseResponse<VideoInfoResp> getVideoInfoExt(@PathVariable String bvid) {
        ArchiveVideoInfo videoInfo = videoInfoMapper.selectByBvid(bvid);
        if (videoInfo == null) {
            return BaseResponse.error(ResponseCode.NOT_FOUND, "视频信息未找到");
        }
        return BaseResponse.ok(new VideoInfoResp(videoInfo, commentMapper.selectCommentAllCountForOid(videoInfo.avid, 1),
                commentMapper.selectRootCommentCountForOid(videoInfo.avid, 1),
                FileUtil.calculateSize(fileService.newBvidDirFile(videoInfo.bvid))));
    }

    @GetMapping("favorite/{favId}")
    public BaseResponse<List<FavoriteVideoInfo>> getFavVideoInfos(
            @PathVariable long favId,
            @RequestParam(defaultValue = "1") int pn,
            @RequestParam(defaultValue = "30") int ps) {

        // 确保页码不小于 1
        pn = Math.max(pn, 1);
        // 计算偏移量
        int offset = (pn - 1) * ps;
        // 查询数据
        List<FavoriteVideoInfo> favoriteVideos = videoFavoriteMapper.selectPageByFavId(favId, offset, ps);
        // 包装为响应对象
        return BaseResponse.ok(favoriteVideos);
    }

    @GetMapping("history/{uid}")
    public BaseResponse<List<HistoryVideoInfo>> getHisVideoInfos(@PathVariable long uid, @RequestParam(defaultValue = "1") int pn,
                                                                 @RequestParam(name = "as_of", required = false) Long asOf,
                                                                 @RequestParam(name = "failed_only", defaultValue = "false") boolean failedOnly) {
        int offset = (pn - 1) * 30;
        return BaseResponse.ok(videoHistoryMapper.selectPageByUid(uid, 30, offset, failedOnly, asOf));
    }

    @GetMapping("uploader/{upUid}")
    public BaseResponse<List<UploaderVideoInfo>> getUPVideoInfos(@PathVariable long upUid, @RequestParam(defaultValue = "1") int pn,
                                                                 @RequestParam(defaultValue = "30") int ps) {
        pn = Math.max(pn, 1);
        // 计算偏移量
        int offset = (pn - 1) * ps;
        // 查询数据
        List<UploaderVideoInfo> uploaderVideoInfos = videoUploaderMapper.selectPageByUploaderId(upUid, offset, ps);
        // 包装为响应对象
        return BaseResponse.ok(uploaderVideoInfos);
    }

    @GetMapping("search")
    public BaseResponse<List<ArchiveVideoInfo>> searchVideos(@RequestParam(required = false) String text) {
        if (text == null || text.length() < 1) {
            return BaseResponse.error(ResponseCode.ILLEGAL_REQUEST, "请输入搜索内容");
        }
        return BaseResponse.ok(videoInfoMapper.searchVideos("%" + text + "%"));
    }

    /**
     * 获取所有存档的视频
     * 但这个烂尾了，在前端没有做任何东西，原因：无法获取总数，如果两次查询，第一次获取总数，第二次获取列表数据，导致两次全表搜索，期望一次
     *
     * @param pn         页码
     * @param ps         页大小
     * @param sortBy     排序依据
     * @param sortType   排序类型
     * @param searchText 搜索文本
     * @return
     */
    @GetMapping("info")
    public BaseResponse<?> getAllVideoInfoByPage(
            @RequestParam(defaultValue = "1") int pn,
            @RequestParam(defaultValue = "30") int ps,
            @RequestParam(name = "sort_by", defaultValue = "1") int sortBy,
            @RequestParam(name = "sort_type", defaultValue = "0") int sortType,
            @RequestParam(name = "search", required = false) String searchText) {

        // 转换排序字段
        String sortByColumn;
        switch (sortBy) {
            case SORT_BY_UPDATE_TIME:
                sortByColumn = "community_update_time";
                break;
            case SORT_BY_UPLOAD_TIME:
                sortByColumn = "ctime";
                break;
            case SORT_BY_SAVE_TIME:
                sortByColumn = "save_time";
                break;
            default:
                return BaseResponse.badRequest("非法sort_by:" + sortBy);
        }

        // 转换排序类型
        String sortOrder = (sortType == SORT_TYPE_ASC) ? "ASC" : "DESC";

        // 计算偏移量
        int offset = (pn - 1) * ps;

        // 处理搜索文本
        String likeText = (searchText != null && !searchText.isEmpty()) ? "%" + searchText + "%" : null;

        // 数据库查询
        List<ArchiveVideoInfo> result = videoInfoMapper.selectBySearch(
                likeText, sortByColumn, sortOrder, offset, ps
        );

        // 返回结果
        return BaseResponse.ok(result);
    }


}
