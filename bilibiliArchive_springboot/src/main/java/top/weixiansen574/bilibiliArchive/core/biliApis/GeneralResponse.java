package top.weixiansen574.bilibiliArchive.core.biliApis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import top.weixiansen574.bilibiliArchive.core.biliApis.BaseResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;

public class GeneralResponse<T> extends BaseResponse {
    public T data;

    @JsonIgnore
    public T getDataNotNull() throws BiliBiliApiException {
        if (!isSuccess() || data == null){
            throw new BiliBiliApiException(code,message,null);
        }
        return data;
    }

    public T getDataNotNull(String errorTips) throws BiliBiliApiException {
        if (!isSuccess() || data == null){
            throw new BiliBiliApiException(code,message,errorTips);
        }
        return data;
    }

    public static class ErrorCodes{
        public static class VideoInfo{
            public static final int FAILED = -404;
            public static final int UP_DELETED = 62002;
            public static final int PRIVATE = 62012;

            public static boolean containsAny(int code){
                return code == FAILED || code == UP_DELETED || code == PRIVATE;
            }
        }
    }
}