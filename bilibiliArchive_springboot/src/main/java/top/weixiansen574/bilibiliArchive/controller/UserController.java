package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.weixiansen574.bilibiliArchive.bean.response.BaseResponse;
import top.weixiansen574.bilibiliArchive.bean.BiliUser;
import top.weixiansen574.bilibiliArchive.bean.response.ResponseCode;
import top.weixiansen574.bilibiliArchive.bean.response.UserRCode;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.exceptions.NotFoundException;
import top.weixiansen574.bilibiliArchive.services.BackupService;
import top.weixiansen574.bilibiliArchive.services.UserService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public UserService userService;
    public BackupService backupService;

    @Autowired
    public UserController(UserService userService, BackupService backupService) {
        this.userService = userService;
        this.backupService = backupService;
    }

    @GetMapping
    public BaseResponse<List<BiliUser>> getAll(){
        return BaseResponse.ok(userService.getAllUser());
    }


    @ResponseBody
    @PostMapping
    public BaseResponse<BiliUser> addFromCookie(@RequestParam String cookie) throws BiliBiliApiException, IOException {
        backupService.checkNotRunning();
        BiliUser biliUser;
        try {
            biliUser = userService.addUserFromCookie(cookie);
        } catch (UserService.UserExistsException e) {
            return new BaseResponse<>(UserRCode.USER_EXISTS,e.getMessage(),e.biliUser);
        }
        if (biliUser == null){
            return BaseResponse.error(UserRCode.ILLEGAL_COOKIE,"Cookie无效");
        }
        return BaseResponse.ok(biliUser);
    }

    @GetMapping("{uid}")
    public BaseResponse<BiliUser> getByUid(@PathVariable long uid){
        BiliUser biliUser = userService.getUserByUid(uid);
        if (biliUser == null) {
            return BaseResponse.error(ResponseCode.NOT_FOUND,"未找到用户");
        }
        return BaseResponse.ok(biliUser);
    }

    @DeleteMapping("{uid}")
    public BaseResponse<Void> deleteUser(@PathVariable long uid){
        if (!userService.checkExists(uid)){
            return BaseResponse.notfound("用户未找到");
        }
        if (userService.checkUserHasReferences(uid)){
            return BaseResponse.error(UserRCode.USER_IS_REFERENCED,"删除失败，删除前请先移除用户所有备份项（例如：收藏夹、历史记录、UP主）");
        }
        userService.deleteUser(uid);
        return BaseResponse.ok(null,"删除成功");
    }

    @PutMapping("{uid}")
    public BaseResponse<BiliUser> update(@PathVariable("uid") long targetUid,@RequestParam String cookie) throws BiliBiliApiException, IOException {
        backupService.checkNotRunning();
        try {
            BiliUser biliUser = userService.updateUser(targetUid, cookie);
            if (biliUser == null){
                return BaseResponse.error(UserRCode.ILLEGAL_COOKIE,"Cookie无效");
            }
            return BaseResponse.ok(biliUser);
        } catch (UserService.UIDInconsistencyException e) {
            return new BaseResponse<>(UserRCode.UID_INCONSISTENCY,"cookie获取到的用户与预期的不一致，预期UID："+
                    targetUid+"，实际UID："+e.biliUser.uid,e.biliUser);
        } catch (NotFoundException e) {
            return BaseResponse.error(ResponseCode.NOT_FOUND,"目标用户不存在");
        }
    }

}
