package top.weixiansen574.bilibiliArchive.core.biliApis;

import retrofit2.Response;

import java.io.IOException;

public class ResponseNullException extends IOException {
    public final Response<?> response;

    public ResponseNullException(Response<?> response) {
        super("Response body was null, response:"+response);
        this.response = response;
    }
}
