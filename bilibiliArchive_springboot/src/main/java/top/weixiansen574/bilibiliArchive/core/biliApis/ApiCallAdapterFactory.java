package top.weixiansen574.bilibiliArchive.core.biliApis;


import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ApiCallAdapterFactory extends CallAdapter.Factory {

    private final WbiFactory wbiFactory = new WbiFactory();

    @Override
    public CallAdapter<?, ?> get(@NotNull Type returnType, @NotNull Annotation[] annotations, @NotNull Retrofit retrofit) {
        // 检查返回类型是否是 ApiCall
        if (getRawType(returnType) != ApiCall.class) {
            return null;
        }

        boolean hasWbiAnnotation = false;
        for (Annotation annotation : annotations) {
            if (annotation instanceof EnableWbi) {
                hasWbiAnnotation = true;
                break;
            }
        }

        // 确认 ApiCall 的泛型参数
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("ApiCall must be parameterized as ApiCall<Foo> or ApiCall<? extends Foo>");
        }

        // 获取 ApiCall 内的泛型参数 T
        Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);

        return new ApiCallAdapter<>(responseType,hasWbiAnnotation ? wbiFactory : null);
    }

    // ApiCallAdapter用于将Retrofit的响应适配为ApiCall类型
    private record ApiCallAdapter<T extends GeneralResponse<R>, R>(Type responseType, WbiFactory wbiFactory)
            implements CallAdapter<T, ApiCall<T, R>> {

        @NotNull
        @Override
        public ApiCall<T, R> adapt(@NotNull Call<T> call) {
            return new ApiCall<>(call,wbiFactory);
        }
    }

/*    @Override
    public CallAdapter<?, ?> get(@NotNull Type returnType, @NotNull Annotation[] annotations, @NotNull Retrofit retrofit) {

        if (getRawType(returnType) == ApiCall.class){
            final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
            return new CallAdapter<Object, ApiCall<?,?>>() {
                @NotNull
                @Override
                public Type responseType() {
                    return responseType;
                }

                @SuppressWarnings("all")
                @NotNull
                @Override
                public ApiCall<?,?> adapt(@NotNull Call<Object> call) {
                    return new ApiCall(call);
                }
            };
        }
        return null;
    }*/
}
