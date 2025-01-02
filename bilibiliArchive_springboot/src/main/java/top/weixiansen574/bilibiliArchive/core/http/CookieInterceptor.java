package top.weixiansen574.bilibiliArchive.core.http;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public record CookieInterceptor(String cookie,String ua) implements Interceptor {

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String url = originalRequest.url().toString();
        String header = originalRequest.header("Cookie");
        if (header == null) {
            Request newRequest = originalRequest.newBuilder()
                    .url(url)
                    .addHeader("Cookie", this.cookie)
                    .addHeader("user-agent",ua)
                    .build();
            return chain.proceed(newRequest);
        } else if (header.equals("")){
            //移除空Cookie，处理412问题会用到
            Request newRequest = originalRequest.newBuilder()
                    .url(url)
                    .removeHeader("Cookie")
                    .addHeader("user-agent",ua)
                    .build();
            return chain.proceed(newRequest);
        }
        return chain.proceed(originalRequest);
    }

}
