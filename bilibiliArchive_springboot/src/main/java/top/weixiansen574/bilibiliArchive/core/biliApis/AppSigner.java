package top.weixiansen574.bilibiliArchive.core.biliApis;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AppSigner {
    private static final String APP_KEY = "1d8b6e7d45233436";
    private static final String APP_SEC = "560c52ccd288fed045859ed18bffd973";

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        params.put("build", "7340200");
        params.put("c_locale", "zh_CN");
        //params.put("channel", "xiaomi_cn_tv.danmaku.bili_20210930");
        params.put("disable_rcmd", "0");
        params.put("fnval", "976");
        params.put("fnver", "0");
        params.put("force_host", "0");
        params.put("fourk", "1");
        params.put("from", "0");
        params.put("local_time", "8");
        params.put("mobi_app", "android");
        params.put("platform", "android");
        params.put("player_net", "1");
        params.put("qn", "32");
        params.put("s_locale", "zh_CN");
        //params.put("statistics", "{\"appId\":1,\"platform\":3,\"version\":\"7.34.0\",\"abtest\":\"\"}");
        params.put("ts", "1732983394");
        params.put("vmid", "75797102");
        System.out.println(appSign(params));
    }

    public static String appSign(Map<String, String> params) {
        // 为请求参数进行 APP 签名
        params.put("appkey", APP_KEY);
        // 按照 key 重排参数
        Map<String, String> sortedParams = new TreeMap<>(params);
        // 序列化参数
        StringBuilder queryBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append('&');
            }
            queryBuilder
                    .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return generateMD5(queryBuilder .append(APP_SEC).toString());
    }

    private static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
