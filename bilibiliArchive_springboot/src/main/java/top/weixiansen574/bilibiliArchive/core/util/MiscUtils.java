package top.weixiansen574.bilibiliArchive.core.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.ArchiveVideoInfo;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.GeneralResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MiscUtils {
    public static final SimpleDateFormat cnSdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.CHINA);
    public static String getEndPathForHttpUrl(String url){
        String[] split = url.split("/");
        return split[split.length - 1];
    }


    public static String omit(String text,int length){
        if (text.length() > length){
            return text.substring(0,length) + "……".replace("\n", " ");
        } else {
            return text;
        }
    }

    public static boolean notNulls(Object... objects){
        for (Object object : objects) {
            if (object == null){
                return false;
            }
        }
        return true;
    }

    public static String formatSeconds(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void sleepNoException(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }


    public static boolean matchOne(Object src, Object... dst) {
        for (Object s : dst) {
            if (src.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 移除图片URL上的@，如果有的话
     * 例如 https://i1.hdslb.com/bfs/face/04bc8e4a18da37d4636f9577cea9e503e136b87e.jpg@128w_128h_1o.webp 多了 @128w_128h_1o.webp，移除这个
     * @param url
     * @return
     */
    public static String cleanUrlAt(String url) {
        // 使用正则表达式匹配 `@` 后面的内容并去除
        return url.replaceAll("@.*", "");
    }



    public static VideoInfo getVideoInfoOrChangeState(UserContext userContext, ArchiveVideoInfo videoInfo) throws IOException, BiliBiliApiException {
        String bvid = videoInfo.bvid;
        BiliApiService biliApiService = userContext.biliApiService;
        OkHttpClient httpClient = userContext.httpClient;
        //遇到失效且未备份，不改变其状态
        if (videoInfo.state.equals(ArchiveVideoInfo.STATE_FAILED_AND_NO_BACKUP)){
            return null;
        }
        GeneralResponse<VideoInfo> resp = biliApiService.getVideoInfoByBvid(bvid).exe();
        String newState = ArchiveVideoInfo.getStateFromApiResponseCode(resp.code);
        if (newState == null){
            throw new BiliBiliApiException(resp,"获取视频状态失败，BV号："+bvid);
        }
        if (newState.equals(ArchiveVideoInfo.STATE_NORMAL)){
            //禁止搜索的检查
            Request request= new Request.Builder().url("https://www.bilibili.com/video/"+bvid)
                    .addHeader("Referer","https://www.baidu.com").build();
            ResponseBody body = OkHttpUtil.getResponseBodyNotNull(httpClient.newCall(request).execute());
            Document document = Jsoup.parse(body.string());
            //可能不严谨。因为这货，视频不见了，响应码却是200😅
            if ("视频去哪了呢？_哔哩哔哩_bilibili".equals(document.title())){
                videoInfo.state = ArchiveVideoInfo.STATE_SEARCH_BAN;
                //不返回null，依旧返回视频信息，视频仅禁止搜索了而已
            } else {
                videoInfo.state = ArchiveVideoInfo.STATE_NORMAL;
            }
            return resp.data;
        }
        //考虑到视频的ShadowBan是一个珍贵的数据，UP主发现后可能会将视频删除，所以旧状态为ShadowBan且新状态不为正常的则使用旧状态
        if (videoInfo.state.equals(ArchiveVideoInfo.STATE_SHADOW_BAN)){
            return null;
        }
        //检查是否是ShadowBan
        if (biliApiService.getVideoPagesByBvid(bvid).exe().code == 0){
            videoInfo.state = ArchiveVideoInfo.STATE_SHADOW_BAN;
            return null;
        }
        videoInfo.state = newState;
        return null;
    }
}
