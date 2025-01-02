package top.weixiansen574.bilibiliArchive.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.weixiansen574.bilibiliArchive.bean.Emote;
import top.weixiansen574.bilibiliArchive.bean.response.BackupRCode;
import top.weixiansen574.bilibiliArchive.bean.response.BaseResponse;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.Emotes;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.core.util.OkHttpUtil;
import top.weixiansen574.bilibiliArchive.mapper.master.EmoteMapper;
import top.weixiansen574.bilibiliArchive.services.FileService;
import top.weixiansen574.bilibiliArchive.services.UserService;
import top.weixiansen574.bilibiliArchive.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/emote")
public class EmoteController {
    UserService userService;
    FileService fileService;
    EmoteMapper emoteMapper;


    public EmoteController(UserService userService, FileService fileService, EmoteMapper emoteMapper) {
        this.userService = userService;
        this.fileService = fileService;
        this.emoteMapper = emoteMapper;
    }

    @GetMapping("")
    public BaseResponse<List<Emote>> getAllEmote() {
        return BaseResponse.ok(emoteMapper.selectAllEmotes());
    }

    @DeleteMapping("{id}")
    public BaseResponse<Boolean> delete(@PathVariable long id) {
        Emote emote = emoteMapper.selectEmoteById(id);
        if (emote == null) {
            return BaseResponse.ok(false);
        }
        if (!fileService.newEmoteFile(emote.fileName).delete()) {
            return BaseResponse.ok(false);
        }
        return BaseResponse.ok(emoteMapper.deleteEmoteById(id));
    }

    @GetMapping("update-list/{uid}")
    public synchronized BaseResponse<Integer> update(@PathVariable long uid) throws BiliBiliApiException, IOException {
        UserContext userContext = userService.getUserContext(uid);
        if (userContext == null){
            return BaseResponse.error(BackupRCode.USER_NOT_EXISTS,"用户不存在");
        }
        int downloaded = 0;
        Emotes emotes = userContext.biliApiService.getEmotes().success();
        for (Emotes.Package aPackage : emotes.packages) {
            if (aPackage.id != 1 && aPackage.id != 2) {
                continue;
            }
            for (Emotes.EmoteInfo emoteInfo : aPackage.emote) {
                if (downloadEmote(userContext.httpClient,emoteInfo)) {
                    downloaded++;
                }
            }
        }
        return BaseResponse.ok(downloaded);
    }

    private boolean downloadEmote(OkHttpClient httpClient,Emotes.EmoteInfo emoteInfo) throws IOException {
        String url = emoteInfo.url;
        String fileName = MiscUtils.getEndPathForHttpUrl(url);
        File file = fileService.newEmoteFile(fileName);
        boolean down = false;
        if (!file.exists()) {
            System.out.println("下载表情："+emoteInfo.text);
            Request request = new Request.Builder().url(url).build();
            ResponseBody responseBody = OkHttpUtil.getResponseBodyNotNull(httpClient.newCall(request).execute());
            FileUtil.outputToFile(responseBody.byteStream(), file);
            down = true;
        }
        Emote emote = emoteMapper.selectEmoteById(emoteInfo.id);
        if (emote != null) {
            if (!emote.fileName.equals(fileName)) {
                FileUtil.deleteOneFile(fileService.newEmoteFile(fileName));
            }
            emote.url = url;
            emote.fileName = fileName;
            emoteMapper.updateEmote(emote);
        } else {
            emote = new Emote(emoteInfo.id, emoteInfo.text, emoteInfo.meta.size, url, fileName);
            emoteMapper.insertEmote(emote);
        }
        return down;
    }

}
