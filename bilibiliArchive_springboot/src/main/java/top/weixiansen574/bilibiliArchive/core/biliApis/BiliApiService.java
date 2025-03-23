package top.weixiansen574.bilibiliArchive.core.biliApis;

import com.alibaba.fastjson2.JSONObject;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

import retrofit2.http.QueryMap;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.*;

import java.util.List;
import java.util.Map;

public interface BiliApiService {

    @GET("/x/web-interface/view")
    ApiCall<GeneralResponse<VideoInfo>, VideoInfo> getVideoInfoByBvid(@Query("bvid") String bvid);

    @GET("/x/space/wbi/acc/info")
    ApiCall<GeneralResponse<UserInfo>, UserInfo> getUserInfo(@Query("mid") long mid);

    @GET("/x/player/playurl")
    ApiCall<GeneralResponse<VideoPlayUrlInfo>, VideoPlayUrlInfo>
    getVideoPlayUrlInfoByAvid(@Query("avid") long avid, @Query("cid") long cid);

    @GET("/x/player/playurl")
    ApiCall<GeneralResponse<VideoPlayUrlInfo>, VideoPlayUrlInfo>
    getVideoPlayUrlInfoByBvid(@Query("bvid") String avid, @Query("cid") long cid, @Query("fnval") int fnval);

    /**
     * 获取评论区根页 [警告⚠⚠]:2024-08-11 API疑似被和谐
     *
     * @param sort 排序方式 时间戳降序：0  点赞降序：1  回复数降序：2
     * @param pn   页码，从1开始
     * @param oid  评论区oid
     * @param type 评论区type 1:视频 ……
     */
    @Deprecated
    @GET("/x/v2/reply")
    ApiCall<GeneralResponse<CommentPage>, CommentPage>
    getCommentPage(@Query("sort") int sort, @Query("pn") int pn, @Query("oid") long oid, @Query("type") int type);

    /**
     * 获取评论区根页(……/reply/main api)
     *
     * @param mode 排序方式
     *             0 3：仅按热度
     *             1：按热度+按时间
     *             2：仅按时间
     * @param next 页码？，从0开始
     * @param oid  评论区oid
     * @param type 评论区type 1:视频 ……
     */
    @GET("/x/v2/reply/main")
    ApiCall<GeneralResponse<MainApiCommentPage>, MainApiCommentPage>
    getCommentPageForMainApi(@Query("mode") int mode, @Query("next") int next, @Query("oid") long oid,
                             @Query("type") int type);

    /**
     * @param mode           排序方式
     *                       0 3：仅按热度
     *                       1：按热度+按时间
     *                       2：仅按时间
     * @param pagination_str 初始：{"offset":""}，然后从响应数据里取：cursor.pagination_reply.prev_offset 往前推进
     * @param oid
     * @param type
     * @return
     */
    @GET("/x/v2/reply/main")
    ApiCall<GeneralResponse<MainApiCommentPage>, MainApiCommentPage>
    getCommentPageForMainApi(@Query("mode") int mode, @Query("pagination_str") String pagination_str,
                             @Query("oid") long oid, @Query("type") int type);

    @GET("/x/v2/reply/main")
    ApiCall<GeneralResponse<MainApiCommentPage>, MainApiCommentPage>
    getCommentPageForMainApi(@Query("mode") int mode, @Query("pagination_str") String pagination_str,
                             @Query("oid") long oid, @Query("type") int type, @Query("ps") int ps);

    @InjectWbi
    @GET("/x/v2/reply/wbi/main")
    ApiCall<GeneralResponse<MainApiCommentPage>,MainApiCommentPage> getCommentPageForWbiMainApi(@Query("mode") int mode, @Query("pagination_str") String pagination_str,
    @Query("oid") long oid, @Query("type") int type);

    @GET("/x/v2/reply/reply")
    ApiCall<GeneralResponse<CommentReplyPage>, CommentReplyPage>
    getCommentReplyPage(@Query("root") long root, @Query("oid") long oid, @Query("pn") int pn, @Query("type") int type);

    @GET("/x/v3/fav/folder/created/list-all")
    ApiCall<GeneralResponse<FavoritesList>, FavoritesList> getFavoritesList(@Query("up_mid") long mid);

    @GET("/x/v3/fav/resource/ids?platform=web")
    ApiCall<GeneralResponse<List<FavoriteVideo>>, List<FavoriteVideo>> getFavoriteVideos(@Query("media_id") long fid);

    @GET("/x/v3/fav/resource/list")
    ApiCall<GeneralResponse<DetailedFavoriteVideoInfosPage>, DetailedFavoriteVideoInfosPage>
    getFavoriteVideosInDetailed(@Query("media_id") long fid, @Query("pn") int pn, @Query("ps") int ps,
                                @Query("type") int type);

    @GET("/x/web-interface/history/cursor")
    ApiCall<GeneralResponse<HistoriesPage>, HistoriesPage>
    getLatestHistories(@Query("ps") int ps, @Query("type") String type, @Query("business") String business);

    @GET("/x/web-interface/history/cursor?type=archive&business=archive")
    ApiCall<GeneralResponse<HistoriesPage>, HistoriesPage> getLatestVideoHistories(@Query("ps") int ps);

    @GET("/x/web-interface/history/cursor")
    ApiCall<GeneralResponse<HistoriesPage>, HistoriesPage>
    getHistories(@Query("ps") int ps, @Query("type") String type, @Query("business") String business,
                 @Query("max") long max, @Query("view_at") long view_at);

    @GET("/x/web-interface/history/cursor?type=archive&business=archive")
    ApiCall<GeneralResponse<HistoriesPage>, HistoriesPage>
    getVideoHistories(@Query("ps") int ps, @Query("max") long max, @Query("view_at") long view_at);

    @GET("//member.bilibili.com//x2/creative/h5/calendar/event?ts=0")
    ApiCall<GeneralResponse<CalendarEvent>, CalendarEvent> getCalendarEvents();

    @GET("/x/player/v2")
    ApiCall<GeneralResponse<VideoPlayerInfo>, VideoPlayerInfo>
    getVideoPlayerInfo(@Query("aid") long aid, @Query("cid") long cid);

    @InjectWbi
    @GET("/x/player/wbi/v2")
    ApiCall<GeneralResponse<VideoPlayerInfo>, VideoPlayerInfo>
    getVideoPlayerInfoByWbi(@Query("aid") long aid, @Query("cid") long cid);

    @GET("/x/web-interface/nav")
    ApiCall<GeneralResponse<Nav>, Nav> getUserNav();

    @InjectWbi
    @GET("/x/space/wbi/arc/search")
    ApiCall<GeneralResponse<UploaderVideoPage>, UploaderVideoPage>
    getUploaderVideoPage(@Header("referer") String referer, @Query("mid") long mid, @Query("pn") int pn);

    @GET("/x/v3/fav/folder/created/list-all")
    ApiCall<GeneralResponse<FavoriteListResponse>, FavoriteListResponse> getFavList(@Query("up_mid") long upUid);

    @GET("/x/v3/fav/folder/info")
    ApiCall<GeneralResponse<FavoriteInfoResponse>,FavoriteInfoResponse> getFavInfo(@Query("media_id") long mediaId);

    @GET("//app.bilibili.com/x/v2/space")
    ApiCall<GeneralResponse<MobileUserSpace>,MobileUserSpace> getMobileUserSpace(@QueryMap Map<String,String> queryMap);

    @GET("/x/tag/archive/tags")
    ApiCall<GeneralResponse<List<VideoTag>>,List<VideoTag>> getVideoTags(@Query("bvid") String bvid);

    @GET("/x/player/pagelist")
    ApiCall<GeneralResponse<List<VideoPage>>,List<VideoPage>> getVideoPagesByBvid(@Query("bvid") String bvid);

    @GET("/x/emote/user/panel/web?business=reply")
    ApiCall<GeneralResponse<Emotes>,Emotes> getEmotes();

    @GET("/x/v2/dm/history/index?type=1")
    ApiCall<GeneralResponse<List<String>>,List<String>> getHistoryDanmakuDates(@Query("oid") long oid,@Query("month") String month);

    @InjectWbi
    @GET("/x/web-interface/wbi/search/all/v2")
    ApiCall<GeneralResponse<SearchResponse>,SearchResponse> search(@Query("keyword") String keyword,@Query("page") int page);

/*    @EnableWbi
    @GET("/x/space/wbi/acc/info")
    ApiCall<GeneralResponse<UserProfile>,UserProfile> getUserProfile(@Header("referer") String referer,@Query("mid") long mid);*/
}
