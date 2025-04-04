package top.weixiansen574.bilibiliArchive.core.biliApis.model;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

public class ServiceGenerator {
    private static Retrofit retrofit;

    public static <T> T createService(Class<T> cls) {
        return (T) getRetrofit().create(cls);
    }

    public static Retrofit getRetrofit(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.bilibili.com")
                    .addConverterFactory(FastJsonConverterFactory.create())
                    .client(new OkHttpClient())
                    .build();
        }
        return retrofit;
    }


}
