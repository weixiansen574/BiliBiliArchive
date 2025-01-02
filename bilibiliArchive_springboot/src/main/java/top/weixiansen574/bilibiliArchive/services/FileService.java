package top.weixiansen574.bilibiliArchive.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.weixiansen574.bilibiliArchive.core.util.FFmpegUtil;
import top.weixiansen574.bilibiliArchive.util.FileUtil;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("ClassCanBeRecord")
@Service
public class FileService {

    private final File root;
    private final File archives;
    private final File backupConfigAvatars;
    private final File videos;
    private final File commentPictures;
    private final File uploaderAvatars;
    private final File bcUserAvatars;
    private final File bcUploaderAvatars;
    private final File cache;
    private final File emote;

    public FileService(
            @Value("${files.root}") File root,
            @Value("${files.archives}") File archives,
            @Value("${files.backup-config-avatars}") File backupConfigAvatars,
            @Value("${files.videos}") File videos,
            @Value("${files.comment-pictures}") File commentPictures,
            @Value("${files.uploader-avatars}") File uploaderAvatars,
            @Value("${files.backup-config-avatars}/user") File bcUserAvatars,
            @Value("${files.backup-config-avatars}/uploader") File bcUploaderAvatars,
            @Value("${files.cache}") File cache,
            @Value("${files.emote}") File emote) {

        this.root = root;
        this.archives = createIfNotExists(archives);
        this.backupConfigAvatars = createIfNotExists(backupConfigAvatars);
        this.videos = createIfNotExists(videos);
        this.commentPictures = createIfNotExists(commentPictures);
        this.uploaderAvatars = createIfNotExists(uploaderAvatars);
        this.bcUserAvatars = createIfNotExists(bcUserAvatars);
        this.bcUploaderAvatars = createIfNotExists(bcUploaderAvatars);
        this.cache = createIfNotExists(cache);
        this.emote = createIfNotExists(emote);
    }

    private File createIfNotExists(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Failed to create directory: " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    public File newCoverFile(String bvid){
        return new File(new File(videos, bvid),"cover.jpg");
    }

    public File newVideoFile(String bvid, long cid){
        return new File(newBvidCidDirFile(bvid,cid), "video.mp4");
    }

    public File newDanmakuFile(String bvid, long cid){
        return new File(newBvidCidDirFile(bvid,cid),"danmaku.xml");
    }

    public File createCoverFile(String bvid) throws IOException {
        File bvidFolder = FileUtil.getOrCreateDir(videos, bvid);
        return new File(bvidFolder, "cover.jpg");
    }

    public File createVideoFile(String bvid, long cid) throws IOException {
        File cidFolder = newAndCreateBvidCidDirFile(bvid, cid);
        return new File(cidFolder, "video.mp4");
    }

    public File createDanmakuFile(String bvid, long cid) throws IOException {
        File cidFolder = newAndCreateBvidCidDirFile(bvid, cid);
        return new File(cidFolder, "danmaku.xml");
    }

    /**
     * 获取视频文件夹，如果没有创建一个
     * @param bvid
     * @param cid
     * @return
     * @throws IOException
     */
    public File newAndCreateBvidCidDirFile(String bvid, long cid) throws IOException {
        File bvidFolder = FileUtil.getOrCreateDir(videos, bvid);
        return FileUtil.getOrCreateDir(bvidFolder, "c_" + cid);
    }

    public File newBvidDirFile(String bvid){
        return new File(videos, bvid);
    }

    public File newBvidCidDirFile(String bvid, long cid){
        return new File(newBvidDirFile(bvid),"c_" + cid);
    }

    public File newUploaderAvatarFile(String fileName) {
        return new File(uploaderAvatars, fileName);
    }

    public File newCommentPictureFile(String fileName) {
        return new File(commentPictures, fileName);
    }

    public File newBCUploaderAvatarFile(String fileName){
        return new File(bcUploaderAvatars,fileName);
    }

    public File newBCUserAvatarFile(String fileName){
        return new File(bcUserAvatars,fileName);
    }

    public File getCache() {
        return cache;
    }

    public File newEmoteFile(String fileName){
        return new File(emote,fileName);
    }

    public void mergeAndSaveVideoFile(String bvid, long cid, File videoFile, File audioFile) throws IOException {
        File mergedVideoFile = createVideoFile(bvid, cid);
        //如果已经存在，要删除，否则ffmpeg是否确认覆盖卡死（更新视频画质的情况）
        if (mergedVideoFile.exists()){
            FileUtil.deleteOneFile(mergedVideoFile);
        }
        FFmpegUtil.merge(audioFile.getAbsolutePath(), videoFile.getAbsolutePath(),
                mergedVideoFile.getAbsolutePath());
        if (!mergedVideoFile.exists()){
            throw new IOException("使用ffmpeg合并了视频，但发现合并后的视频文件"+mergedVideoFile+"并不存在，请尝试检查ffmpeg或硬盘");
        }
    }

    public void saveNoAudioVideoFile(String bvid, long cid, File videoFile) throws IOException {
        File cidFolder = newBvidCidDirFile(bvid, cid);
        if (videoFile.renameTo(new File(cidFolder, "video.mp4"))) {
            throw new IOException("移动无音频视频文件失败");
        }
    }

    public File getCommentPictures() {
        return commentPictures;
    }

    public long calcVideoAllDiskUsage(String bvid){
        return FileUtil.calculateSize(newBvidDirFile(bvid));
    }

    @Override
    public String toString() {
        return "FileService{" +
                "root=" + root +
                ", archives=" + archives +
                ", backupConfigAvatars=" + backupConfigAvatars +
                ", videos=" + videos +
                ", commentPictures=" + commentPictures +
                ", uploaderAvatars=" + uploaderAvatars +
                ", bcUserAvatars=" + bcUserAvatars +
                ", bcUploaderAvatars=" + bcUploaderAvatars +
                '}';
    }
}
