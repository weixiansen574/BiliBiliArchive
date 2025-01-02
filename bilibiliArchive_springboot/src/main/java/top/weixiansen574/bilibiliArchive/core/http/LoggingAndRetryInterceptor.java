package top.weixiansen574.bilibiliArchive.core.http;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志&重试拦截器
 * 响应体为JSON时打印响应体
 * 仅JSON数据100%实现重试，其他blob数据在取流过程发生的如断连异常，请添加自己的处理办法
 */
public class LoggingAndRetryInterceptor implements Interceptor {
    HttpLogger logger;
    int maxRetries;

    public LoggingAndRetryInterceptor(HttpLogger logger, int maxRetries) {
        this.logger = logger;
        this.maxRetries = maxRetries;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        for (int tryCount = 0; tryCount <= maxRetries; tryCount++) {
            try {
                response = chain.proceed(request);
                // 打印请求信息
                String time = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
                log("========Http log "+time+" =========");
                log("Request URL: " + request.url());
                log("Request Method: " + request.method());
                // 打印请求头
                log("Request Headers: " + request.headers());
                // 打印请求体
                RequestBody requestBody = request.body();
                if (requestBody != null) {
                    if (requestBody instanceof FormBody formBody) {
                        StringBuilder sb = new StringBuilder("Request Body:");
                        for (int i = 0; i < formBody.size() - 1; i++) {
                            sb.append(formBody.encodedName(i)).append("=").append(formBody.value(i)).append("&");
                        }
                        sb.append(formBody.encodedName(formBody.size() - 1))
                                .append("=")
                                .append(formBody.value(formBody.size() - 1));
                        sb.append("\n");
                        log(sb.toString());
                    }
                }
                // 打印响应信息
                log("Response Code: " + response.code());
                log("Response Message: " + response.message());

                // 打印响应头
                log("Response Headers: " + response.headers());

                // 打印响应体 (仅在响应数据是 JSON 时打印)
                ResponseBody body = response.body();
                if (body != null) {
                    MediaType contentType = body.contentType();
                    if (contentType != null && contentType.toString().contains("json")) {
                        String responseBody = body.string();
                        log("Response JSON Data: " + responseBody);
                        // 由于 OkHttp 的 ResponseBody 只能读取一次，所以在打印后需要重新构建一个 Response 并返回
                        log("===========End http log===========");
                        return response.newBuilder()
                                .body(ResponseBody.create(contentType, responseBody))
                                .build();
                    }
                }
                log("===========End http log===========");
                break;
                // 请求成功，不再重试
            } catch (IOException e) {
                System.err.println("正在重试（"+maxRetries+"/"+tryCount+"），在请求URL:"+request.url()+" 时发生："+
                        e.getMessage()+" 异常");
                if (tryCount == maxRetries) {
                    throw e;
                }
                MiscUtils.sleepNoException(1000);
            }
        }
        if (response == null) throw new AssertionError();
        return response;
    }
    
    private void log(String message){
        if (logger != null){
            logger.log(message);
        }
    }
}
