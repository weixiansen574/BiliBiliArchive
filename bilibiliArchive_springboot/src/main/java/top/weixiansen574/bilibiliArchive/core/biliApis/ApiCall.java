package top.weixiansen574.bilibiliArchive.core.biliApis;


import okhttp3.HttpUrl;
import okhttp3.Request;
import okio.Timeout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.TreeMap;

public final class ApiCall<T extends GeneralResponse<R>, R> implements Call<T> {
    private Call<T> delegate;
    private final WbiFactory wbiFactory;

    public ApiCall(Call<T> delegate, WbiFactory wbiFactory) {
        this.delegate = delegate;
        this.wbiFactory = wbiFactory;
    }

    @NotNull
    @Override
    public Response<T> execute() throws IOException {
        // 可以在这里对请求结果进行自定义处理
        // 对 response 进行自定义处理
        if (wbiFactory != null) {
            try {
                Call<T> delegateClone = delegate.clone();
                Class<?> clazz = delegate.getClass();
                Field rawCallField = clazz.getDeclaredField("rawCall");
                Field executedField = clazz.getDeclaredField("executed");
                rawCallField.setAccessible(true);
                executedField.setAccessible(true);
                okhttp3.Call.Factory callFactory = getCallFactory();
                rawCallField.set(delegate, callFactory.newCall(requestAddWbi(wbiFactory, callFactory)));
                Response<T> response = delegate.execute();
                //如果wbi失效，尝试刷新一次，再返回重试的请求结果
                //错误号:412
                //由于触发哔哩哔哩安全风控策略，该次访问请求被拒绝。

                //这好像并不是Wbi引起的原因？
                if (response.code() == 412) {
                    delegate = delegateClone;
                    wbiFactory.refresh(callFactory);
                    rawCallField.set(delegate, callFactory.newCall(requestAddWbi(wbiFactory, callFactory)));
                    executedField.set(delegate, false);
                    return delegate.execute();
                }
                if (response.body() == null) {
                    return response;
                }
                //响应体JSON：code:-352  message:风控校验失败
                T body = response.body();
                if (body.code == -352 && "风控校验失败".equals(body.message)) {
                    delegate = delegateClone;
                    wbiFactory.refresh(callFactory);
                    rawCallField.set(delegate, callFactory.newCall(requestAddWbi(wbiFactory, callFactory)));
                    executedField.set(delegate, false);
                    return delegate.execute();
                }
                return response;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            //Response<T> response = delegate.clone().execute();
            //请求评论回复API的时候无法预测的出现412错误码问题，给我搞头大了，但是使劲重试总能过的😂
            Response<T> execute = delegate.clone().execute();
            if (execute.code() != 412){
                return execute;
            }
            //若出现412就干它1000次直到成功
            for (int i = 0; i < 1000; i++) {
                try {
                    Call<T> clone = delegate.clone();
                    Request rawReq = clone.request();
                    HttpUrl tsUrl = rawReq.url().newBuilder()
                            //加时间戳，
                            .addQueryParameter("ts", String.valueOf(System.currentTimeMillis()))
                            .build();
                    Request request = rawReq.newBuilder().url(tsUrl)
                            .addHeader("Connection", "close")
                            .build();
                    Class<?> clazz = clone.getClass();
                    Field rawCallField = clazz.getDeclaredField("rawCall");
                    rawCallField.setAccessible(true);
                    rawCallField.set(clone, getCallFactory().newCall(request));
                    Response<T> response = clone.execute();
                    if (response.code() == 412) {
                        PG.content("收到412错误，等3秒后重试，%d/1000，response信息：%s",i,response);
                        MiscUtils.sleepNoException(3000);
                    } else {
                        return response;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            //TODO 还失败就移除cookie重试
            try {
                System.out.println("重试失败，移除Cookie最后尝试一遍");
                Call<T> clone = delegate.clone();
                Request request = clone.request().newBuilder().addHeader("Cookie", "").build();
                Class<?> clazz = clone.getClass();
                Field rawCallField = clazz.getDeclaredField("rawCall");
                rawCallField.setAccessible(true);
                rawCallField.set(clone, getCallFactory().newCall(request));
                return clone.execute();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            //return delegate.clone().execute();
        }
    }

    @Override
    public void enqueue(@NotNull Callback<T> callback) {
        // 可以在这里自定义异步处理逻辑
        delegate.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
                // 对 response 进行自定义处理
                callback.onResponse(ApiCall.this, response);
            }

            @Override
            public void onFailure(@NotNull Call<T> call, @NotNull Throwable t) {
                // 对错误进行自定义处理
                callback.onFailure(ApiCall.this, t);
            }
        });
    }

    @Override
    public boolean isExecuted() {
        return delegate.isExecuted();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isCanceled() {
        return delegate.isCanceled();
    }

    @SuppressWarnings("all")
    @NotNull
    @Override
    public ApiCall<T, R> clone() {
        return new ApiCall<>(delegate.clone(), wbiFactory);
    }

    @NotNull
    @Override
    public Request request() {
        return delegate.request();
    }

    public Request requestAddWbi(WbiFactory wbiFactory, okhttp3.Call.Factory factory) throws IOException {
        Request request = request();
        HttpUrl url = request.url();
        TreeMap<String, Object> map = new TreeMap<>();
        for (String name : url.queryParameterNames()) {
            map.put(name, url.queryParameter(name));
        }
        long wts = System.currentTimeMillis() / 1000;
        map.put("wts", wts);
        Wbi wbi = wbiFactory.getWbiOrFresh(factory);
        HttpUrl newUrl = url.newBuilder()
                .addQueryParameter("wts", String.valueOf(wts))
                .addQueryParameter("w_rid", wbi.wbiSign(map))
                .build();
        return request.newBuilder().url(newUrl).build();
    }


    @NotNull
    @Override
    public Timeout timeout() {
        return delegate.timeout();
    }

    @NotNull
    public T exe() throws IOException {
        Response<T> response = execute();
        T body = response.body();
        if (body == null) {
            throw new ResponseNullException(response);
        } else {
            return body;
        }
    }

    @Nullable
    public R data() throws IOException {
        return exe().data;
    }

    public R success() throws IOException, BiliBiliApiException {
        return success(null);
    }

    public R success(String errorTips) throws IOException, BiliBiliApiException {
        T body = exe();
        if (body.isSuccess()) {
            return body.data;
        } else {
            throw new BiliBiliApiException(body, errorTips);
        }
    }

    private okhttp3.Call.Factory getCallFactory() {
        try {
            Class<?> clazz = delegate.getClass();
            Field callFactoryField = clazz.getDeclaredField("callFactory");
            callFactoryField.setAccessible(true);
            return (okhttp3.Call.Factory) callFactoryField.get(delegate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ApiCall[" +
                "delegate=" + delegate + ", " +
                "wbiFactory=" + wbiFactory + ']';
    }


}

