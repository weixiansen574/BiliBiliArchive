package top.weixiansen574.bilibiliArchive.core.downloaders;

import okhttp3.OkHttpClient;
import top.weixiansen574.bilibiliArchive.bean.ArchiveComment;
import top.weixiansen574.bilibiliArchive.bean.config.CommentDownloadConfig;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.CommentDownloadInterface;
import top.weixiansen574.bilibiliArchive.core.biliApis.GeneralResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.*;
import top.weixiansen574.bilibiliArchive.core.http.ResponseNotSuccessfulException;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.mapper.comment.AvatarMapper;
import top.weixiansen574.bilibiliArchive.mapper.comment.CommentMapper;
import top.weixiansen574.bilibiliArchive.services.FileService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CommentDownloader extends ContentDownloader implements CommentDownloadInterface {

    protected final CommentMapper commentMapper;
    protected final AvatarMapper avatarMapper;

    public CommentDownloader(OkHttpClient httpClient, BiliApiService biliApiService, FileService fileService, CommentMapper commentMapper, AvatarMapper avatarMapper) {
        super(httpClient, biliApiService, fileService);
        this.commentMapper = commentMapper;
        this.avatarMapper = avatarMapper;
    }

    /**
     * @return 评论区总楼层 特殊情况 -- 评论区已关闭：0 精选评论：-1
     */
    public int downloadOrUpdateComment(long oid, int type, CommentDownloadConfig config) throws BiliBiliApiException, IOException {
        if (config == null) {
            return 0;
        }
        //保存与更新评论点赞，统称“存档”
        //置顶评论：任何配置都存档
        //置顶评论的回复：所有、热门时存档，增量、爬楼时仅在数据库没有该置顶评论时存档

        //获取第一页按时间评论，获取置顶保存，检查评论区是否精选（是否支持按时间排序）
        GeneralResponse<MainApiCommentPage> resp = biliApiService.getCommentPageForWbiMainApi(MODE_SORT_TIME, PaginationStr.INITIAL, oid, type).exe();
        if (resp.code == 12061 || resp.code == 12002) {
            //评论区已关闭
            PG.content("因为：" + resp.message + "，跳过评论下载");
            return 0;
        } else if (!resp.isSuccess()) {
            throw new BiliBiliApiException(resp, "获取首评论页失败");
        }
        int savedRootCount = 0;
        int savedReplyCount = 0;
        //在保存置顶评论前先查询本地存有的评论数量，以便后面对于处理精选评论下时间排序依赖的问题
        int existingCount = commentMapper.selectCommentAllCountForOid(oid, type);
        MainApiCommentPage page = resp.data;
        savedRootCount += saveTopComments(page);
        if (config.mode.equals(CommentDownloadConfig.MODE_ALL)) {
            savedReplyCount += saveTopCommentReplies(page, false, CommentDownloadConfig.REPLY_MODE_ALL, -1);
        } else if (config.mode.equals(CommentDownloadConfig.MODE_HOT)) {
            savedReplyCount += saveTopCommentReplies(page, false, config.replyMode, config.replyLimit);
        } else {
            //增量、爬楼时仅在数据库没有该置顶评论时存档，saveRepliesIfItNotExists=true
            savedReplyCount += saveTopCommentReplies(page, true, config.replyMode, config.replyLimit);
        }
        int floor;
        if (!page.cursor.support_mode.contains(MODE_SORT_TIME)) {
            //如果遭遇评论区开精选的情况，【楼层嗅探】【增量更新】配置将无法正常工作！当且仅当数据库里没有评论时进行全部评论下载（仅对更新拒绝下载评论）
            floor = -1;
            if (config.mode.equals(CommentDownloadConfig.MODE_LATEST_FIRST) || config.mode.equals(CommentDownloadConfig.MODE_FLOOR_SNIFFER)) {
                if (existingCount > 0) {
                    PG.content("当前评论区开启了如精选评论的选项，无法按时间排序。你的配置依赖时间排序，所以无法执行评论更新");
                    sleepNoException(5000);
                } else {
                    PG.content("当前评论区开启了如精选评论的选项，无法按时间排序。你的配置依赖时间排序，但由于首次保存评论区，下载所有评论");
                    sleepNoException(5000);
                    normalDownload(page, oid, type, MODE_SORT_HOT, CommentDownloadConfig.REPLY_MODE_ALL, -1,
                            -1, 0, savedRootCount, savedReplyCount);
                }
                return floor;
            }
        } else {
            floor = page.cursor.prev - 1;
        }
        switch (config.mode) {
            case CommentDownloadConfig.MODE_ALL -> normalDownload(page, oid, type, floor != -1 ? MODE_SORT_TIME : MODE_SORT_HOT,
                    CommentDownloadConfig.REPLY_MODE_ALL, -1, -1, 0, savedRootCount, savedReplyCount);
            case CommentDownloadConfig.MODE_HOT -> normalDownload(page, oid, type, MODE_SORT_HOT, config.replyMode, config.replyLimit,
                    config.rootLimit, 0, savedRootCount, savedReplyCount);
            case CommentDownloadConfig.MODE_LATEST_FIRST -> {
                ArchiveComment latest = commentMapper.selectLatestPostedRootComment(oid, type);
                normalDownload(page, oid, type, MODE_SORT_TIME, CommentDownloadConfig.REPLY_MODE_ALL, -1, -1,
                        latest != null ? latest.ctime : 0, savedRootCount, savedReplyCount);
            }
            case CommentDownloadConfig.MODE_FLOOR_SNIFFER -> sniffFloorDownload(page, floor,oid, type);
        }
        return floor;
    }

    private void normalDownload(MainApiCommentPage page, long oid, int type, int sortMode, String replyMode, int replyLimit,
                                int rootLimit, long ctMark, int savedRootCount, int savedReplyCount) throws BiliBiliApiException, IOException {
        //如果不是按时间排序的需要重新获取一遍page
        if (sortMode != MODE_SORT_TIME) {
            page = biliApiService.getCommentPageForWbiMainApi(sortMode, PaginationStr.INITIAL, oid, type).success();
        }
        int totalCount = page.cursor.all_count;
        flipPage:
        while (true) {
            List<BiliComment> comments = page.replies;
            //翻页到没有评论的时候停止翻页
            if (comments == null || comments.size() == 0) {
                break;
            }
            for (BiliComment comment : comments) {
                saveCommentAndAvatarAndPictures(comment);
                savedReplyCount += downloadReplies(comment, replyMode, replyLimit);
                savedRootCount++;
                PG.content("已下载评论[%d:%d/%d][%s][%s]", savedRootCount,
                        savedRootCount + savedReplyCount, totalCount, comment.getUname(),
                        MiscUtils.omit(comment.getMessage(), 30));
                //下载的根评论数量满足要求后退出翻页
                if (rootLimit != -1 && savedRootCount >= rootLimit) {
                    PG.content("下载的根评论数量已达到[%d/%d]，完成下载！%n", savedRootCount, rootLimit);
                    break flipPage;
                }
                if (comment.ctime <= ctMark) {
                    PG.content("下载的评论已到时间标记点位置[时间标记点：%s <= 当前评论：%s]，完成下载！%n",
                            MiscUtils.cnSdf.format(ctMark * 1000), MiscUtils.cnSdf.format(comment.ctime * 1000));
                    break flipPage;
                }
            }
            if (page.cursor.pagination_reply == null || page.cursor.pagination_reply.next_offset == null) {
                break;
            }
            page = biliApiService.getCommentPageForWbiMainApi(sortMode,
                    new PaginationStr(page.cursor.pagination_reply.next_offset).toJson(), oid, type).success();
        }
        PG.content("评论下载完毕");
    }

    public void sniffFloorDownload(MainApiCommentPage page, int totalFloor,long oid, int type) throws IOException, BiliBiliApiException {
        int floorMark = 0;
        ArchiveComment markComment = commentMapper.selectTopFloorRootComment(oid, type);
        if (markComment != null && markComment.floor != null){
            floorMark = markComment.floor;
        }
        BiliComment comment;
        int cFloor;
        if (page.replies != null) {
            comment = page.replies.get(0);
            cFloor = totalFloor;
        } else {
            return;
        }
        String pStr;
        for (int i = totalFloor - 1; i > 0; i--) {
            pStr = "{\"offset\":\"{\\\"type\\\":3,\\\"direction\\\":1,\\\"Data\\\":{\\\"cursor\\\":" + i + "}}\"}";
            page = biliApiService.getCommentPageForWbiMainApi(MODE_SORT_TIME, pStr, oid, type).success();
            List<BiliComment> replies = page.replies;
            if (replies != null) {
                if (replies.isEmpty()) {
                    break;
                }
                BiliComment currentComment = replies.get(0);
                if (currentComment.rpid != comment.rpid) {
                    PG.content("[#%d][%s][%s]",cFloor,comment.getUname(),MiscUtils.omit(comment.getMessage(),30));
                    saveCommentAndAvatarAndPictures(comment, cFloor);
                    downloadReplies(comment, CommentDownloadConfig.REPLY_MODE_ALL,-1);
                    comment = currentComment;
                } else {
                    PG.content("[#%d?][%s][%s]",cFloor,comment.getUname(),MiscUtils.omit(comment.getMessage(),30));
                }
                cFloor = i;
                try {
                    //降低api调用速率
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
            if (i <= floorMark) {
                return;
            }
        }
        PG.content("[#%d][%s][%s]",cFloor,comment.getUname(),MiscUtils.omit(comment.getMessage(),30));
        saveCommentAndAvatarAndPictures(comment, cFloor);
        downloadReplies(comment, CommentDownloadConfig.REPLY_MODE_ALL,-1);
    }

    public int downloadReplies(BiliComment rootComment, String mode, Integer limit) throws IOException, BiliBiliApiException {
        if (mode.equals(CommentDownloadConfig.REPLY_MODE_INCREMENT)) {
            return downloadRepliesByIncrement(rootComment);
        } else {
            if (mode.equals(CommentDownloadConfig.REPLY_MODE_ALL)) {
                return downloadRepliesByLimit(rootComment, -1);
            } else if (mode.equals(CommentDownloadConfig.REPLY_MODE_COUNT_LIMIT)) {
                return downloadRepliesByLimit(rootComment, limit);
            } else {
                throw new IllegalArgumentException("非法的评论回复下载模式：" + mode);
            }
        }
    }

    /**
     * 增量更新模式，回复列表是时间正序，需要反向翻页直到日期<=标记点停止
     */
    private int downloadRepliesByIncrement(BiliComment rootComment) throws BiliBiliApiException, IOException {
        int count = 0;
        int replyCount = rootComment.rcount;
        for (int pn = replyCount / 10 + 1; pn > 0; pn--) {
            ArchiveComment latest = commentMapper.selectLatestPostedReplyComment(rootComment.oid, rootComment.type, rootComment.rpid);
            long ctMark = latest != null ? latest.ctime : 0;
            List<BiliComment> replies = getCommentReplyPage(rootComment,pn);
            if (replies == null){
                break;
            }
            for (int i = replies.size() - 1; i >= 0; i--) {
                BiliComment reply = replies.get(i);
                if (reply.ctime <= ctMark) {
                    return count;
                }
                PG.content("正在下载评论回复[%d/%d][%s][%s]", count + 1, rootComment.rcount, reply.getUname(),
                        MiscUtils.omit(reply.getMessage(), 30));
                saveCommentAndAvatarAndPictures(reply);
                count++;
            }
        }
        return count;
    }

    private int downloadRepliesByLimit(BiliComment rootComment, int limit) throws IOException, BiliBiliApiException {
        int count = 0;
        //如果评论回复预览评论的数量与评论声明的回复数量一致，则直接在预览列表中取，节约流量和时间
        if (rootComment.replies != null && rootComment.replies.size() == rootComment.rcount) {
            return savePreviewReplies(rootComment, limit);
        }
        for (int pn = 1; pn <= rootComment.rcount / 10 + 1; pn++) {
            List<BiliComment> replies = getCommentReplyPage(rootComment,pn);
            if (replies == null){
                break;
            }
            for (BiliComment reply : replies) {
                PG.content("正在下载评论回复[%d/%d][%s][%s]", count + 1, rootComment.rcount, reply.getUname(),
                        MiscUtils.omit(reply.getMessage(), 30));
                saveCommentAndAvatarAndPictures(reply);
                count++;
                if (limit != -1 && count >= limit) {
                    return count;
                }
            }
        }
        return count;
    }

    /**
     * 保存回复预览列表中的评论
     *
     * @param rootComment 根评论对象
     * @param limit       数量限制 为-1不做限制
     * @return 下载的评论数量
     */
    private int savePreviewReplies(BiliComment rootComment, int limit) throws IOException {
        int count = 0;
        for (BiliComment reply : rootComment.replies) {
            if (limit == -1 || count < limit) {
                saveCommentAndAvatarAndPictures(reply);
                count++;
            } else {
                return count;
            }
        }
        return count;
    }

    private int saveTopComments(CommentPage cPage) throws IOException {
        List<BiliComment> topReplies = cPage.top_replies;
        int count = 0;
        if (topReplies != null && !topReplies.isEmpty()) {
            for (BiliComment topReply : topReplies) {
                PG.content("置顶评论[%s][%s]", topReply.getUname(),
                        MiscUtils.omit(topReply.getMessage(), 30));
                saveCommentAndAvatarAndPictures(topReply);
                count++;
            }
        } else {
            PG.content("无置顶评论");
        }
        return count;
    }

    private int saveTopCommentReplies(CommentPage cPage, boolean saveRepliesIfItNotExists, String mode, Integer limit) throws IOException, BiliBiliApiException {
        List<BiliComment> topReplies = cPage.top_replies;
        int count = 0;
        if (topReplies != null) {
            for (BiliComment topReply : topReplies) {
                if (!saveRepliesIfItNotExists || commentMapper.checkExists(topReply.rpid)) {
                    count += downloadReplies(topReply, mode, limit);
                }
            }
        }
        return count;
    }

    private void saveCommentAndAvatarAndPictures(BiliComment comment, Integer floor) throws IOException {
        synchronized (ContentDownloader.class) {
            //下载头像
            String avatarUrl = MiscUtils.cleanUrlAt(comment.member.avatar);
            //头像文件名，也就是URL路径后面的那串文件名
            String avatarName = MiscUtils.getEndPathForHttpUrl(avatarUrl);
            //检查是否已下载过头像，若没有，则下载
            if (!avatarMapper.checkExists(avatarName)) {
                try {
                    avatarMapper.insert(MiscUtils.getEndPathForHttpUrl(avatarUrl), downloadContentAndRetry(
                            createDownloadRequest(avatarUrl + "@160w_160h_1c_1s_!web-avatar-comment.avif"), 15));
                } catch (ResponseNotSuccessfulException e) {
                    if (e.response.code() == 404) {
                        PG.content("用户头像[" + avatarUrl + "]404了，跳过下载");
                    } else {
                        throw e;
                    }
                }
            }
            //保存评论到数据库
            insertCommentOrUpdateLike(comment, floor);
        }
        downloadCommentPicturesIfNotExists(comment);
    }


    private void saveCommentAndAvatarAndPictures(BiliComment comment) throws IOException {
        saveCommentAndAvatarAndPictures(comment, null);
    }

    /**
     * 下载评论图片,跳过已下载的
     *
     * @throws IOException 重试多遍还未下载成功
     */
    private void downloadCommentPicturesIfNotExists(BiliComment comment) throws IOException {
        String[] pictures = comment.getPictureUrls();
        if (pictures != null) {
            for (String pUrl : pictures) {
                File file = fileService.newCommentPictureFile(MiscUtils.getEndPathForHttpUrl(pUrl));
                if (!file.exists()) {
                    PG.content("下载图片：" + pUrl);
                    try {
                        downloadContentAndRetry(createDownloadRequest(pUrl), file, 10);
                    } catch (IOException e) {
                        if (e instanceof ResponseNotSuccessfulException exception) {
                            PG.content("下载图片：" + pUrl + " 收到错误码：" + exception.response.code()
                                    + " 可能图片在图床上已失效，跳过下载");
                            exception.response.close();
                        }
                    }
                } else {
                    PG.content("图片：" + pUrl + "下载过，已跳过！");
                }
            }
        }
    }

    private List<BiliComment> getCommentReplyPage(BiliComment rootComment,int pn) throws IOException, BiliBiliApiException {
        GeneralResponse<CommentReplyPage> response = biliApiService.getCommentReplyPage(rootComment.rpid,
                rootComment.oid, pn, rootComment.type).exe();
        //下到一半，根评论GG了
        if (response.code == 12022){//“已经被删除了”
            PG.content("根评论GG了！跳过下载");
            return null;
        }
        if (response.isSuccess()){
            List<BiliComment> replies = response.data.replies;
            if (replies == null || replies.size() == 0) {
                return null;
            }
            return replies;
        } else {
            throw new BiliBiliApiException(response,"获取评论回复列表失败");
        }
    }

    private void insertCommentOrUpdateLike(BiliComment comment, Integer floor) {
        if (commentMapper.checkExists(comment.rpid)) {
            if (floor == null) {
                commentMapper.updateLike(comment.like, comment.rpid);
            } else {
                commentMapper.updateLikeAndFloor(comment.like, floor, comment.rpid);
            }
        } else {
            commentMapper.insert(new ArchiveComment(comment, floor));
        }
    }


    public static void sleepNoException(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }


}
