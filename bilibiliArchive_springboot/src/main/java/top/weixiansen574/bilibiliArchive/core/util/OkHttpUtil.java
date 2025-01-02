package top.weixiansen574.bilibiliArchive.core.util;

import okhttp3.ResponseBody;

import java.io.IOException;

public class OkHttpUtil {

    public static ResponseBody getResponseBodyNotNull(okhttp3.Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null){
            throw new IOException("response body is null,response:"+response);
        }
        return body;
    }


    /*public static void responseObjNotNull(Object o) throws IOException {
        if (o == null){
            throw new IOException("response is null");
        }
    }*/
}
