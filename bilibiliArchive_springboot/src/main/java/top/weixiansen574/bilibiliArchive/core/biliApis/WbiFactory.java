package top.weixiansen574.bilibiliArchive.core.biliApis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.Nav;
import top.weixiansen574.bilibiliArchive.core.http.ResponseNotSuccessfulException;
import top.weixiansen574.bilibiliArchive.core.util.OkHttpUtil;

import java.io.IOException;
import java.time.LocalDate;

public class WbiFactory {
    private Wbi wbi;
    private LocalDate lastRefreshDate;

    public void refresh(okhttp3.Call.Factory factory) throws IOException {
        Request request = new Request.Builder()
                .url("https://api.bilibili.com/x/web-interface/nav")
                .build();
        Response response = factory.newCall(request).execute();
        if (!response.isSuccessful()){
            response.close();
            throw new ResponseNotSuccessfulException(response);
        }
        ResponseBody responseBody = OkHttpUtil.getResponseBodyNotNull(response);
        JSONObject jsonObject = JSON.parseObject(responseBody.string());

        Nav nav = jsonObject.getObject("data", Nav.class);
        this.lastRefreshDate = LocalDate.now();
        wbi = new Wbi(nav);
    }

    public synchronized Wbi getWbiOrFresh(okhttp3.Call.Factory factory) throws IOException {
        if (wbi == null || !LocalDate.now().equals(lastRefreshDate)){//若天数发生变化，则刷新一次Wbi
            refresh(factory);
        }
        return wbi;
    }
}
