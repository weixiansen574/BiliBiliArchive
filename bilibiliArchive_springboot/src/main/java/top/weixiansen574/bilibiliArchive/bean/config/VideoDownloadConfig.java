package top.weixiansen574.bilibiliArchive.bean.config;

import top.weixiansen574.bilibiliArchive.core.biliApis.VideoQualityInfo;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

import java.util.Objects;

public class VideoDownloadConfig implements VideoQualityInfo {
    public int clarity;
    public int codecId;

    public VideoDownloadConfig() {
    }

    public VideoDownloadConfig(int clarity, int codecId) {
        this.clarity = clarity;
        this.codecId = codecId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoDownloadConfig that = (VideoDownloadConfig) o;
        return clarity == that.clarity && codecId == that.codecId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clarity, codecId);
    }

    public static VideoDownloadConfig getDefault(){
        return new VideoDownloadConfig(QN_1080P,CODEC_ID_H265);
    }

    public boolean check(){
        if (!MiscUtils.matchOne(clarity,QN_240P,QN_360P,QN_480P,QN_720P,QN_720P_60F,QN_1080P,QN_1080P_HQ,
                QN_1080P_60F,QN_4K,QN_HDR,QN_DOLBY_HORIZON,QN_8K)) {
            return false;
        }
        return MiscUtils.matchOne(codecId, CODEC_ID_H265, CODEC_ID_H264);
    }

    @Override
    public String toString() {
        return "VideoDownloadConfig{" +
                "clarity=" + clarity +
                ", codecId=" + codecId +
                '}';
    }

}
