package top.weixiansen574.bilibiliArchive.core.http;

import okhttp3.Response;

import java.io.IOException;

public class ResponseNotSuccessfulException extends IOException {
    public final Response response;

    public ResponseNotSuccessfulException(Response response) {
        super("Response not successful:"+response);
        this.response = response;
    }
}
