package top.weixiansen574.bilibiliArchive.core.biliApis;

import org.jetbrains.annotations.Nullable;


public class BiliBiliApiException extends Exception{
    public int code;
    public String message;
    public String tips;

    public BiliBiliApiException(GeneralResponse<?> response, @Nullable String tips){
        this(response.code,response.message,tips);
    }

    public BiliBiliApiException(int code, String message,@Nullable String tips) {
        super(formatMessage(code,message,tips));
        this.code = code;
        this.message = message;
        this.tips = tips;
    }

    private static String formatMessage(int code, String message,@Nullable String tips){
        String msg;
        if (tips == null){
            msg = String.format("code:%d  message:%s",code,message);
        } else {
            msg = String.format("code:%d  message:%s  Tips:%s",code,message,tips);
        }
        return msg;
    }
}
