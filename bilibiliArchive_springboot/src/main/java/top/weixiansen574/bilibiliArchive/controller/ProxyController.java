package top.weixiansen574.bilibiliArchive.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("proxy")
public class ProxyController {

/*    OkHttpClient okHttpClient = new OkHttpClient();

    @GetMapping("get")
    public ResponseEntity<InputStreamResource> get
            (@RequestParam String url, @RequestParam(required = false) List<String> headers,
             @RequestParam(required = false) Long cookie_uid,@RequestParam(required = false) Long enable_wbi) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        ResponseBody body = response.body();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.code());
        String contentType = response.header("Content-Type");
        if (contentType != null){
            builder.contentType(MediaType.parseMediaType(contentType));
        }
        if (response.isSuccessful()){
            if (body != null){
                return builder.body(new InputStreamResource(body.byteStream()));
            }
            return builder.body(null);
        }
        if (body != null){
            return builder.body(new InputStreamResource(body.byteStream()));
        }
        return builder.body(null);
    }*/
//url=xxx&header=xxx
}
