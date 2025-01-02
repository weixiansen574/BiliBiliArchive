package top.weixiansen574.bilibiliArchive.core.downloaders;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;
import top.weixiansen574.bilibiliArchive.bean.Danmaku;
import top.weixiansen574.bilibiliArchive.bean.Subtitle;
import top.weixiansen574.bilibiliArchive.bean.list.VideoTagList;
import top.weixiansen574.bilibiliArchive.bean.videoinfo.DownloadedVideoPage;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.SubtitleInfo;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoInfo;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoPage;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.VideoPlayUrlInfo;
import top.weixiansen574.bilibiliArchive.core.http.ResponseNotSuccessfulException;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.mapper.comment.AvatarMapper;
import top.weixiansen574.bilibiliArchive.mapper.comment.CommentMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.SubtitleMapper;
import top.weixiansen574.bilibiliArchive.services.FileService;
import top.weixiansen574.bilibiliArchive.util.FileUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoDownloader extends CommentDownloader {

    private final SubtitleMapper subtitleMapper;

    public VideoDownloader(OkHttpClient httpClient, BiliApiService biliApiService, FileService fileService, CommentMapper commentMapper, AvatarMapper avatarMapper, SubtitleMapper subtitleMapper) {
        super(httpClient, biliApiService, fileService, commentMapper, avatarMapper);
        this.subtitleMapper = subtitleMapper;
    }

    /**
     * 下载视频，BV号下的所有分p视频（纯视频流&音频流，最后合并）
     * @param oldDownloadedVideoPages 以前下载的视频分p列表，如果有相符且画质编码一致的则跳过下载,节省时间流量
     */
    public List<DownloadedVideoPage> downloadOrUpdateVideo(
            VideoInfo videoInfo, @Nullable List<DownloadedVideoPage> oldDownloadedVideoPages,
            int videoMaxQn, int videoCodec, int audioMaxBandwidth, boolean isVip) throws IOException, BiliBiliApiException {

        String bvid = videoInfo.bvid;
        long avid = videoInfo.aid;
        List<VideoPage> pages = videoInfo.pages;
        List<DownloadedVideoPage> downloadedVideoPages = new ArrayList<>();
        if (videoInfo.pages == null) {
            return downloadedVideoPages;
        }
        File bvidCacheDir = createBvidCacheDir(bvid);
        for (VideoPage page : pages) {
            long cid = page.cid;
            VideoPlayUrlInfo videoPlayUrlInfo = biliApiService.getVideoPlayUrlInfoByBvid(bvid, cid, 4048)
                    .success("获取视频PlayUrl失败");
            VideoPlayUrlInfo.Video dashVideo = videoPlayUrlInfo.getEligibleDashVideo(videoMaxQn, videoCodec, isVip);
            if (dashVideo == null){
                throw new IOException("画质列表与视频链接列表不匹配，请检查cookie是否失效？");
            }
            DownloadedVideoPage downloadedVideoPage = new DownloadedVideoPage
                    (page, dashVideo.codecid, dashVideo.id, dashVideo.width, dashVideo.height);
            if (checkVideoPageNeedDownload(oldDownloadedVideoPages,cid,dashVideo)) {
                PG.content("下载cid=" + cid + "的视频流……");
                File videoCacheFile = downloadVideoStream(dashVideo, bvid, cid);
                PG.content("下载cid=" + cid + "的音频流……");
                File audioCacheFile = downloadAudioStream(videoPlayUrlInfo.getEligibleDashAudio(audioMaxBandwidth), bvid, cid);
                if (audioCacheFile != null){
                    fileService.mergeAndSaveVideoFile(bvid,cid,videoCacheFile,audioCacheFile);
                } else {
                    fileService.saveNoAudioVideoFile(bvid,cid,videoCacheFile);
                }
                List<SubtitleInfo> subtitles = biliApiService.getVideoPlayerInfoByWbi(avid,cid)
                        .success("获取字幕失败").subtitle.subtitles;
                //OkHttpUtil.getData(biliApiService.getVideoPlayerInfo(avid, cid)).subtitle.subtitles;
                for (SubtitleInfo info : subtitles) {
                    if (!subtitleMapper.checkExists(info.id)){
                        PG.content("下载字幕："+info.lan_doc);
                        String content = downloadStringAndRetry(createDownloadRequest("https:" + info.subtitle_url),10);
                        subtitleMapper.insert(new Subtitle(info,content));
                    }
                }
                downloadedVideoPage.subtitles = subtitles;
            } else {
                //的视频与存在画质与将要下载的一致，跳过下载
                PG.content("cid=" + cid + "的视频与存档库中的编码与画质一致，跳过下载");
            }
            downloadedVideoPages.add(downloadedVideoPage);
        }
        FileUtil.deleteDirs(bvidCacheDir);
        return downloadedVideoPages;
    }


    public void downloadCover(String coverUrl, String bvid) throws IOException {
        PG.content("正在下载封面");
        Request request = createDownloadRequestFromTheVideo(coverUrl, bvid);
        File coverFile = fileService.createCoverFile(bvid);
        downloadContentAndRetry(request, coverFile, 15);
    }

    /**
     * 下载或更新已存在的弹幕，如果是优先级重载的，那么就当做更新啦，更新不会删除旧的弹幕
     * @param videoInfo
     * @return
     */
    public void downloadOrUpdateDanmaku(VideoInfo videoInfo) throws IOException {
        String bvid = videoInfo.bvid;
        for (VideoPage page : videoInfo.pages) {
            long cid = page.cid;
            PG.content("下载cid:"+cid+" 的弹幕");
            downloadDMByProtobuf(fileService.createDanmakuFile(bvid,cid),cid,bvid,page.duration);
        }
    }

    private void downloadDMByProtobuf(File danmakuFile, long cid, String bvid, int videoLength) throws IOException{

        //下载弹幕并存到内存
        List<Danmaku> danmakuList = new ArrayList<>();
        for (int segment_index = 1; segment_index <= videoLength / 360 + 1; segment_index++) {
            byte[] bytes = null;
            try{
                bytes = downloadContentAndRetry(createDownloadRequestFromTheVideo(
                        "https://api.bilibili.com/x/v2/dm/web/seg.so?type=1&oid=" +
                                cid + "&segment_index=" + segment_index, bvid),15);
            } catch (ResponseNotSuccessfulException e){
                if (e.response.code() == 304){
                    PG.content("获取弹幕时收到304响应码，跳过此次");
                } else {
                    throw e;
                }
            }
            if (bytes != null){
                danmakuList.addAll(Danmaku.parseFromProtobuf(bytes));
            }
        }

        List<Danmaku> oldDanmakuList = null;
        if (danmakuFile.exists()) {
            try {
                oldDanmakuList = Danmaku.paresFromXML(new FileInputStream(danmakuFile));
            } catch (SAXException | ParserConfigurationException e) {
                e.printStackTrace();
                File newFile = new File(danmakuFile.getParent(), "danmaku_backup_" +
                        System.currentTimeMillis() + ".xml");
                FileUtil.copyFile(danmakuFile,newFile);
                FileUtil.deleteOneFile(danmakuFile);
                System.out.println("以前的弹幕文件损坏了，更新弹幕将重建文件，旧的弹幕文件备份至："+newFile);
            }
        }

        //保存到文件或者更新
        try {
            if (oldDanmakuList != null) {
                danmakuList = Danmaku.merge(danmakuList,oldDanmakuList);
            }
            Danmaku.toXML(danmakuList,cid,new FileOutputStream(danmakuFile));
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException(e);
        }

    }

    protected File downloadVideoStream(VideoPlayUrlInfo.Video dashVideo, String bvid, long cid) throws IOException {

        for (int i = 0; i < dashVideo.backupUrl.size(); i++) {
            try {
                return downloadVideoStream(dashVideo.backupUrl.get(i), bvid, cid);
            } catch (IOException ignored) {
            }
        }
        //最后一个还是失败就扔异常
        return downloadVideoStream(dashVideo.baseUrl, bvid, cid);
    }

    protected File downloadAudioStream(VideoPlayUrlInfo.Audio dashAudio, String bvid, long cid) throws IOException {
        //一些特殊的视频没有音频流
        if (dashAudio == null){
            return null;
        }
        for (int i = 0; i < dashAudio.backupUrl.size(); i++) {
            try {
                return downloadAudioStream(dashAudio.backupUrl.get(i), bvid, cid);
            } catch (IOException ignored) {
            }
        }
        return downloadAudioStream(dashAudio.baseUrl, bvid, cid);
    }

    protected boolean checkVideoPageNeedDownload(List<DownloadedVideoPage> pages, long cid, VideoPlayUrlInfo.Video dashVideo) {
        if (pages == null) {
            return true;
        }
        for (DownloadedVideoPage page : pages) {
            if (page.cid == cid) {
                return page.qn != dashVideo.id || page.codecId != dashVideo.codecid;
            }
        }
        return true;
    }

    protected File createBvidCacheDir(String bvid) throws IOException {
        return FileUtil.getOrCreateDir(cachePath, bvid);
    }


    protected File downloadVideoStream(String url, String bvid, long cid) throws IOException {
        Request request = createDownloadRequestFromTheVideo(url, bvid);
        File videoStreamFile = new File(cachePath, bvid + "/" + "video_" + cid + ".m4s");
        downloadContentAndRetry(request, videoStreamFile, 3);
        return videoStreamFile;
    }

    protected File downloadAudioStream(String url, String bvid, long cid) throws IOException {
        Request request = createDownloadRequestFromTheVideo(url, bvid);
        File audioStreamFile = new File(cachePath, bvid + "/" + "audio_" + cid + ".m4s");
        downloadContentAndRetry(request, audioStreamFile, 3);
        return audioStreamFile;
    }

    protected static Request createDownloadRequestFromTheVideo(String url, String refBvid) {
        return createDownloadRequest(url, "https://www.bilibili.com/video/" + refBvid);
    }

    public VideoTagList getVideoTags(String bvid) throws BiliBiliApiException, IOException {
        return new VideoTagList(biliApiService.getVideoTags(bvid).success("获取视频TAG列表失败"));
    }
}
