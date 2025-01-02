package top.weixiansen574.bilibiliArchive.core.biliApis;

public interface VideoQualityInfo {
    int CODEC_ID_H264 = 7;
    int CODEC_ID_H265 = 12;
    int CODEC_ID_AV01 = 13;

    int QN_240P = 6;
    int QN_360P = 16;
    int QN_480P = 32;
    int QN_720P = 64;
    int QN_720P_60F = 74;
    int QN_1080P = 80;
    int QN_1080P_HQ = 112;
    int QN_1080P_60F = 116;
    int QN_4K = 120;
    int QN_HDR = 125;
    int QN_DOLBY_HORIZON = 126;
    int QN_8K = 127;
}
