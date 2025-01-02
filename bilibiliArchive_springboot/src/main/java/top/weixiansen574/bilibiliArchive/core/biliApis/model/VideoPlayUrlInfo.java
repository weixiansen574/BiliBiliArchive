package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import top.weixiansen574.bilibiliArchive.core.biliApis.VideoQualityInfo;

import java.util.List;

public class VideoPlayUrlInfo implements VideoQualityInfo {

    public String from;
    public String result;
    public String message;
    public int quality;
    public String format;
    public long timelength;
    public String accept_format;
    public List<String> accept_description;
    public List<Integer> accept_quality;
    public int video_codecid;
    public String seek_param;
    public String seek_type;
    public Dash dash;
    public List<Support_formats> support_formats;
    public String high_format;
    public long last_play_time;
    public long last_play_cid;


    public static class Dash {

        public int duration;
        public double minBufferTime;
        public double min_buffer_time;
        public List<Video> video;
        public List<Audio> audio;
        public Dolby dolby;
        public String flac;

    }

    public static class Video {

        public int id;
        public String baseUrl;
        public String base_url;
        public List<String> backupUrl;
        public List<String> backup_url;
        public long bandwidth;
        public String mimeType;
        public String mime_type;
        public String codecs;
        public int width;
        public int height;
        public String frameRate;
        public String frame_rate;
        public String sar;
        public int startWithSap;
        public int start_with_sap;
        public SegmentBase SegmentBase;
        public Segment_base segment_base;
        public int codecid;
    }

    public static class Audio {

        public int id;
        public String baseUrl;
        public String base_url;
        public List<String> backupUrl;
        public List<String> backup_url;
        public long bandwidth;
        public String mimeType;
        public String mime_type;
        public String codecs;
        public int width;
        public int height;
        public String frameRate;
        public String frame_rate;
        public String sar;
        public int startWithSap;
        public int start_with_sap;
        public SegmentBase SegmentBase;
        public Segment_base segment_base;
        public int codecid;
    }

    public static class SegmentBase {
        public String Initialization;
        public String indexRange;
    }

    public static class Segment_base {
        public String initialization;
        public String index_range;
    }

    public static class Dolby {
        public int type;
        public String audio;
    }

    public static class Support_formats {
        public int quality;
        public String format;
        public String new_description;
        public String display_desc;
        public String superscript;
        public List<String> codecs;
    }



    /*public VideoUrlInfo getVideoUrl(int maxQn, int codecId) {
        for (Video video : dash.video) {
            if (video.id <= maxQn) {
                if (video.codecid == codecId) {
                    return new VideoUrlInfo(video.baseUrl,video.codecid,video.id);
                }
            }
        }
        //若没有该编码格式则使用比他小一级的
        for (Video video : dash.video) {
            if (video.id <= maxQn) {
                if (video.codecid <= codecId) {
                    return new VideoUrlInfo(video.baseUrl,video.codecid,video.id);
                }
            }
        }
        //如果清晰度设置得过小则选择最小的
        Video video = dash.video.get(dash.video.size() - 1);
        return new VideoUrlInfo(video.baseUrl,video.codecid,video.id);
    }

    public List<VideoUrlInfo> getVideoBackupUrl(int maxQn, int codecId) {
        for (Video video : dash.video) {
            if (video.id <= maxQn) {
                if (video.codecid == codecId) {
                    List<VideoUrlInfo> videoUrlInfos = new ArrayList<>();
                    for (String url : video.backupUrl) {
                        videoUrlInfos.add(new VideoUrlInfo(url,video.codecid,video.id));
                    }
                    return videoUrlInfos;
                }
            }
        }
        //若没有该编码格式则使用比他小一级的
        for (Video video : dash.video) {
            if (video.id <= maxQn) {
                if (video.codecid <= codecId) {
                    List<VideoUrlInfo> videoUrlInfos = new ArrayList<>();
                    for (String url : video.backupUrl) {
                        videoUrlInfos.add(new VideoUrlInfo(url,video.codecid,video.id));
                    }
                    return videoUrlInfos;
                }
            }
        }
        Video video = dash.video.get(dash.video.size() - 1);
        List<VideoUrlInfo> videoUrlInfos = new ArrayList<>();
        for (String url : video.backupUrl) {
            videoUrlInfos.add(new VideoUrlInfo(url,video.codecid,video.id));
        }
        return videoUrlInfos;
    }

    public List<VideoUrlInfo> getAllVideoUrls(int maxQn, int codecId) {
        List<VideoUrlInfo> urlList = new ArrayList<>();
        urlList.add(getVideoUrl(maxQn, codecId));
        List<VideoUrlInfo> videoBackupUrl = getVideoBackupUrl(maxQn, codecId);
        if (videoBackupUrl != null){
            urlList.addAll(videoBackupUrl);
        }
        return urlList;
    }
*/

    public Audio getEligibleDashAudio(int maxBandwidth){
        if (dash.audio != null) {
            for (Audio audio : dash.audio) {
                if (audio.bandwidth <= maxBandwidth) {
                    return audio;
                }
            }
        }
        return null;
    }

    public Video getEligibleDashVideo(int maxQuality, int codecId, boolean isVip){
        int eligibleVideoQuality = getEligibleVideoQuality(maxQuality, isVip);
        Video videoUrls = getCompletelyMatchedVideoUrls(eligibleVideoQuality, codecId);
        if (videoUrls != null){
            return videoUrls;
        }
        return getMatchedQualityVideoUrls(eligibleVideoQuality);
    }

    public int getEligibleVideoQuality(int maxQuality, boolean isVip) {
        if (maxQuality > QN_1080P && !isVip) {
            maxQuality = QN_1080P;
            System.out.println("你不是大会员，但是使用了高于1080p普通画质的选项，降到1080p普通画质选项");
        }
        //accept_quality总是降序排序，从大到小
        for (Integer quality : accept_quality) {
            if (quality <= maxQuality) {
                return quality;
            }
        }
        //如果清晰度设置得太小，就选画质最差的那个
        return accept_quality.get(accept_quality.size() - 1);
    }

    /**
     * 获取完全符合要求的DashURL信息
     * @param quality
     * @param codecId
     */
    public Video getCompletelyMatchedVideoUrls(int quality, int codecId) {
        for (Video video : dash.video) {
            if (video.id == quality && video_codecid == codecId) {
                return video;
            }
        }
        return null;
    }

    /**
     * 获取仅清晰度符合清晰度要求的视频URL，当获取不到取清晰度且编码一致的URLS的时候调用
     * 仅返回一种编码 h264 -> h265 -> av1
     */
    public Video getMatchedQualityVideoUrls(int quality) {
        Video video = getCompletelyMatchedVideoUrls(quality, CODEC_ID_H264);
        if (video != null){
            return video;
        }
        video = getCompletelyMatchedVideoUrls(quality,CODEC_ID_H265);
        if (video != null){
            return video;
        }
        video = getCompletelyMatchedVideoUrls(quality,CODEC_ID_AV01);
        return video;
    }



    public String getAudioBaseUrl(int maxBandwidth) {
        if (dash.audio != null) {
            for (Audio audio : dash.audio) {
                if (audio.bandwidth <= maxBandwidth) {
                    return audio.baseUrl;
                }
            }
        }
        return null;
    }

/*    public List<String> getAudioBackupUrl(int maxBandwidth) {
        if (dash.audio != null) {
            for (Audio audio : dash.audio) {
                if (audio.bandwidth <= maxBandwidth) {
                    return audio.backupUrl;
                }
            }
        }
        return null;
    }*/

/*    @NotNull
    public List<String> getAllAudioUrls(int maxBandwidth) {
        List<String> urlList = new ArrayList<>();
        String audioUrl = getAudioUrl(maxBandwidth);
        List<String> audioBackupUrl = getAudioBackupUrl(maxBandwidth);
        //会有一些视频罕见的没有音频，所以要做准备
        if (audioUrl != null) {
            urlList.add(audioUrl);
        }
        if (audioBackupUrl != null) {
            urlList.addAll(audioBackupUrl);
        }
        return urlList;
    }*/


/*    public record VideoUrlInfo(List<String> urls, int codecId, int qn) {
    }*/
}
