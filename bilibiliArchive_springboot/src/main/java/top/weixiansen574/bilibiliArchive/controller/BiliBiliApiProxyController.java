package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.weixiansen574.bilibiliArchive.bean.response.ApiProxyRCode;
import top.weixiansen574.bilibiliArchive.bean.response.BaseResponse;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.FavoriteInfoResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.FavoriteListResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.GeneralResponse;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.MobileUserSpace;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.UserProfile;
import top.weixiansen574.bilibiliArchive.services.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static top.weixiansen574.bilibiliArchive.core.biliApis.AppSigner.appSign;

@RestController
@RequestMapping("/api/bili-api-proxy")
public class BiliBiliApiProxyController {

    public UserService userService;

    @Autowired
    public BiliBiliApiProxyController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("x/v3/fav/folder/created/list-all")
    public BaseResponse<GeneralResponse<FavoriteListResponse>> getFolderList(@RequestParam("up_mid") long upMid, @RequestParam("cookie_uid") long cookieUid) throws IOException {
        UserContext userContext = userService.getUserContext(cookieUid);
        if (userContext == null){
            return BaseResponse.error(ApiProxyRCode.USER_NOT_FOUND,"cookie_uid对应的用户不存在");
        }
        return BaseResponse.ok(userContext.biliApiService.getFavList(upMid).exe());
    }

    @GetMapping("x/v3/fav/folder/info")
    public BaseResponse<GeneralResponse<FavoriteInfoResponse>> getFavInfo(@RequestParam("media_id") long mediaId,@RequestParam("cookie_uid") long cookieUid) throws IOException {
        UserContext userContext = userService.getUserContext(cookieUid);
        if (userContext == null){
            return BaseResponse.error(ApiProxyRCode.USER_NOT_FOUND,"cookie_uid对应的用户不存在");
        }
        return BaseResponse.ok(userContext.biliApiService.getFavInfo(mediaId).exe());
    }

    @GetMapping("x/v2/space")
    public BaseResponse<GeneralResponse<MobileUserSpace>> getMobileUserSpace(@RequestParam String vmid, @RequestParam("cookie_uid") long cookieUid) throws IOException {
        UserContext userContext = userService.getUserContext(cookieUid);
        if (userContext == null){
            return BaseResponse.error(ApiProxyRCode.USER_NOT_FOUND,"cookie_uid对应的用户不存在");
        }
        Map<String, String> params = new HashMap<>();
        params.put("build", "7340200");
        params.put("c_locale", "zh_CN");
        params.put("disable_rcmd", "0");
        params.put("fnval", "976");
        params.put("fnver", "0");
        params.put("force_host", "0");
        params.put("fourk", "1");
        params.put("from", "0");
        params.put("local_time", "8");
        params.put("mobi_app", "android");
        params.put("platform", "android");
        params.put("player_net", "1");
        params.put("qn", "32");
        params.put("s_locale", "zh_CN");
        params.put("ts", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("vmid",vmid);
        params.put("sign",appSign(params));
        return BaseResponse.ok(userContext.biliApiService.getMobileUserSpace(params).exe());
    }

}
