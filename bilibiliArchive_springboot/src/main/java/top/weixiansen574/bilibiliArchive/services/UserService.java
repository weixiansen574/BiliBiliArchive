package top.weixiansen574.bilibiliArchive.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import top.weixiansen574.bilibiliArchive.bean.BiliUser;
import top.weixiansen574.bilibiliArchive.core.UserContext;
import top.weixiansen574.bilibiliArchive.core.biliApis.ApiCallAdapterFactory;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliApiService;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.CalendarEvent;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.Pfs;
import top.weixiansen574.bilibiliArchive.core.http.CookieInterceptor;
import top.weixiansen574.bilibiliArchive.core.http.LoggingAndRetryInterceptor;
import top.weixiansen574.bilibiliArchive.core.util.MiscUtils;
import top.weixiansen574.bilibiliArchive.core.util.OkHttpUtil;
import top.weixiansen574.bilibiliArchive.exceptions.NotFoundException;
import top.weixiansen574.bilibiliArchive.mapper.comment.AvatarMapper;
import top.weixiansen574.bilibiliArchive.mapper.comment.CommentMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.*;
import top.weixiansen574.bilibiliArchive.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper mapper;
    private final LoggingAndRetryInterceptor interceptor;
    private final FileService fileService;

    private final VideoInfoMapper videoInfoMapper;
    private final CommentMapper commentMapper;
    private final AvatarMapper avatarMapper;
    private final SubtitleMapper subtitleMapper;
    public final VideoFavoriteMapper videoFavoriteMapper;
    public final VideoHistoryMapper videoHistoryMapper;
    public final VideoUploaderMapper videoUploaderMapper;
    public final ArchiveDeleteService archiveDeleteService;
    private final String ua;

    private final Map<Long,UserContext> userContextMap = new HashMap<>();



    @Autowired
    public UserService(UserMapper mapper, LoggingAndRetryInterceptor interceptor, FileService fileService,
                       VideoInfoMapper videoInfoMapper, CommentMapper commentMapper, AvatarMapper avatarMapper,
                       SubtitleMapper subtitleMapper, VideoFavoriteMapper videoFavoriteMapper,
                       VideoHistoryMapper videoHistoryMapper, VideoUploaderMapper videoUploaderMapper,
                       ArchiveDeleteService archiveDeleteService, @Value("${http-req.user-agent}") String ua) {
        this.mapper = mapper;
        this.interceptor = interceptor;
        this.fileService = fileService;
        this.videoInfoMapper = videoInfoMapper;
        this.commentMapper = commentMapper;
        this.avatarMapper = avatarMapper;
        this.subtitleMapper = subtitleMapper;
        this.videoFavoriteMapper = videoFavoriteMapper;
        this.videoHistoryMapper = videoHistoryMapper;
        this.videoUploaderMapper = videoUploaderMapper;
        this.archiveDeleteService = archiveDeleteService;
        this.ua = ua;

        for (BiliUser biliUser : mapper.selectAll()) {
            userContextMap.put(biliUser.uid,createUserContext(biliUser));
        }
    }

    private OkHttpClient createClient(String cookie){
        return new OkHttpClient().newBuilder()
                .addInterceptor(new CookieInterceptor(cookie,ua))
                .addInterceptor(interceptor)
                .build();
    }

    private Retrofit createRetrofit(OkHttpClient okHttpClient){
        return new Retrofit.Builder()
                .baseUrl("https://api.bilibili.com")
                .addCallAdapterFactory(new ApiCallAdapterFactory())
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    private UserContext createUserContext(BiliUser user, OkHttpClient client, BiliApiService biliApiService){
        return new UserContext(user,client,biliApiService,fileService, videoInfoMapper, videoFavoriteMapper,
                videoHistoryMapper, videoUploaderMapper, commentMapper, avatarMapper,subtitleMapper, archiveDeleteService);
    }

    private UserContext createUserContext(BiliUser user){
        if (user == null){
            return null;
        }
        OkHttpClient client = createClient(user.cookie);
        Retrofit retrofit = createRetrofit(client);
        BiliApiService biliApiService = retrofit.create(BiliApiService.class);
        return createUserContext(user,client,biliApiService);
    }

    private UserContext createUserContext(String cookie) throws BiliBiliApiException, IOException {
        OkHttpClient okHttpClient = createClient(cookie);
        Retrofit retrofit = createRetrofit(okHttpClient);
        BiliApiService biliApiService = retrofit.create(BiliApiService.class);
        CalendarEvent calendarEvent = biliApiService.getCalendarEvents().success("获取用户信息失败");
        Pfs pfs = calendarEvent.pfs;
        if (pfs == null){
            //输入的cookie未解析到用户
            return null;
        }
        BiliUser biliUser = new BiliUser(cookie,pfs);
        return createUserContext(biliUser,okHttpClient,biliApiService);
    }

    public BiliUser addUserFromCookie(String cookie) throws BiliBiliApiException, IOException, UserExistsException {
        UserContext userContext = createUserContext(cookie);
        if (userContext == null){
            //输入的cookie未解析到用户
            return null;
        }
        BiliUser biliUser = userContext.biliUser;
        BiliUser existUser = mapper.selectByUid(biliUser.uid);
        if (existUser != null){
            throw new UserExistsException(existUser);
        }
        OkHttpClient okHttpClient = userContext.httpClient;
        String avatarUrl = biliUser.avatarUrl;
        //保存用户的头像到文件
        File avatarFile = fileService.newBCUserAvatarFile(MiscUtils.getEndPathForHttpUrl(avatarUrl));
        Request request = new Request.Builder()
                .url(avatarUrl)
                .build();
        ResponseBody responseBody = OkHttpUtil.getResponseBodyNotNull(okHttpClient.newCall(request).execute());
        FileUtil.outputToFile(responseBody.bytes(),avatarFile);
        mapper.insert(biliUser);
        userContextMap.put(biliUser.uid, userContext);
        return biliUser;
    }

    public BiliUser updateUser(long targetUid,String cookie) throws BiliBiliApiException, IOException, UIDInconsistencyException, NotFoundException {
        BiliUser oldUser = mapper.selectByUid(targetUid);
        if (oldUser == null){
            throw new NotFoundException();
        }
        UserContext userContext = createUserContext(cookie);
        if (userContext == null){
            //输入的cookie未解析到用户
            return null;
        }
        BiliUser biliUser = userContext.biliUser;
        if (biliUser.uid != targetUid){
            throw new UIDInconsistencyException(biliUser);
        }
        //保存用户的头像到文件（如果文件发生变化）
        OkHttpClient okHttpClient = userContext.httpClient;
        String avatarUrl = biliUser.avatarUrl;
        File avatarFile = fileService.newBCUserAvatarFile(MiscUtils.getEndPathForHttpUrl(avatarUrl));
        File oldAvatarFile = fileService.newBCUserAvatarFile(MiscUtils.getEndPathForHttpUrl(oldUser.avatarUrl));
        if (!oldAvatarFile.equals(avatarFile)){
            //删除旧头像文件
            FileUtil.deleteOneFile(oldAvatarFile);
            Request request = new Request.Builder()
                    .url(avatarUrl)
                    .build();
            ResponseBody responseBody = OkHttpUtil.getResponseBodyNotNull(okHttpClient.newCall(request).execute());
            FileUtil.outputToFile(responseBody.bytes(),avatarFile);
        }
        mapper.update(biliUser);
        userContextMap.put(biliUser.uid, userContext);
        return biliUser;
    }

    public void deleteUser(long uid){
        BiliUser biliUser = mapper.selectByUid(uid);
        if (biliUser != null){
            fileService.newBCUserAvatarFile(MiscUtils.getEndPathForHttpUrl(biliUser.avatarUrl));
            mapper.deleteByUid(uid);
            userContextMap.remove(uid);
        }
    }

    public List<BiliUser> getAllUser(){
        return mapper.selectAll();
    }

    public BiliUser getUserByUid(long uid){
        return mapper.selectByUid(uid);
    }

    public boolean checkExists(long uid){
        return mapper.checkExists(uid);
    }

    public void setGlobalVipUser(Long uid){
        UserContext publicUser = getUserContext(uid);
        for (Map.Entry<Long, UserContext> entry : userContextMap.entrySet()) {
            entry.getValue().setPublicVipUserContext(publicUser);
        }
    }

    public UserContext getUserContext(Long uid){
        return userContextMap.get(uid);
    }

    public boolean checkUserHasReferences(long uid) {
        return mapper.checkUserHasReferences(uid);
    }

    public static class UIDInconsistencyException extends Exception{
        public final BiliUser biliUser;

        public UIDInconsistencyException(BiliUser biliUser) {
            this.biliUser = biliUser;
        }
    }

    public static class UserExistsException extends Exception{
        public final BiliUser biliUser;

        public UserExistsException(BiliUser biliUser) {
            super(String.format("用户：%s(%s) 已存在",biliUser.name,biliUser.uid));
            this.biliUser = biliUser;
        }
    }
}
