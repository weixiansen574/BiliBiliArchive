package top.weixiansen574.bilibiliArchive.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import top.weixiansen574.bilibiliArchive.services.FileService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("backup-config-avatars/user/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getBCUserAvatar(@PathVariable String fileName,@RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newBCUserAvatarFile(fileName),download);
    }

    @GetMapping("backup-config-avatars/uploader/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getBCUploaderAvatar(@PathVariable String fileName,@RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newBCUploaderAvatarFile(fileName),download);
    }

    @GetMapping("archives/videos/{bvid:.+}/cover")
    public ResponseEntity<InputStreamResource> getVideoCover(@PathVariable String bvid,@RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newCoverFile(bvid),download);
    }

    @GetMapping("archives/videos/{bvid:.+}/cover/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getChangedVideoCover(@PathVariable String bvid,@PathVariable String fileName,@RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newChangedCoverFile(bvid,fileName),download);
    }

    @GetMapping("archives/videos/{bvid:.+}/staff-avatars/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getStaffAvatar(@PathVariable String bvid,@PathVariable String fileName,
                                                              @RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newStaffAvatarFile(bvid,fileName),download);
    }

    @GetMapping(value = "archives/videos/{bvid:.+}/{cid:.+}/video", produces = "video/webm")
    public void getVideoVideo(@RequestHeader HttpHeaders headers, HttpServletResponse response,
                              @PathVariable String bvid, @PathVariable long cid,
                              @RequestParam(required = false) String filename) {
        File videoFile = fileService.newVideoFile(bvid, cid);
        try {
            if (!videoFile.exists() || !videoFile.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(videoFile, "r");
            List<HttpRange> ranges = headers.getRange();
            long fileLength = videoFile.length();

            // 设置默认文件名
            if (filename == null || filename.isEmpty()) {
                filename = String.format("%s-c_%s.mp4", bvid, cid);
            }
            ContentDisposition contentDisposition = ContentDisposition
                    .attachment()
                    .filename(filename, StandardCharsets.UTF_8)
                    .build();

            if (ranges.isEmpty()) {
                // 没有 Range 请求头，返回整个文件
                response.setStatus(HttpStatus.OK.value());
                response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength));
                response.setHeader(HttpHeaders.CONTENT_TYPE, "video/webm");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

                writeContent(randomAccessFile, response, 0, fileLength - 1);
            } else {
                // 处理 Range 请求头
                HttpRange range = ranges.get(0);
                long start = range.getRangeStart(fileLength);
                long end = range.getRangeEnd(fileLength);
                long contentLength = end - start + 1;

                response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
                response.setHeader(HttpHeaders.CONTENT_TYPE, "video/webm");
                response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
                response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
                response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength);
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

                writeContent(randomAccessFile, response, start, end);
            }
        } catch (IOException ignored) {
        }
    }

    @GetMapping("archives/videos/{bvid:.+}/{cid:.+}/danmaku")
    public ResponseEntity<InputStreamResource> getVideoDanmaku(@PathVariable String bvid,@PathVariable long cid,@RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newDanmakuFile(bvid,cid),download,false);
    }

    @GetMapping("archives/comment-pictures/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getCommentPicture(@PathVariable String fileName,@RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newCommentPictureFile(fileName),download);
    }

    @GetMapping("archives/uploader-avatars/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getArchiveUploaderAvatar(@PathVariable String fileName,@RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newUploaderAvatarFile(fileName),download);
    }

    @GetMapping("emote/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getEmote(@PathVariable String fileName,@RequestParam(defaultValue = "false") boolean download){
        return downloadFile(fileService.newEmoteFile(fileName),download);
    }

    private ResponseEntity<InputStreamResource> downloadFile(File file,boolean download){
        return downloadFile(file,download, true);
    }

    private ResponseEntity<InputStreamResource> downloadFile(File file,boolean download,boolean cacheEnable) {
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(fileInputStream);
        HttpHeaders headers = new HttpHeaders();

        if (download){
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        }

        // 自动检测文件的 Content-Type
        Path filePath = file.toPath();
        String mimeType;
        try {
            mimeType = Files.probeContentType(filePath);
        } catch (IOException e) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 如果检测失败，使用默认值
        }

        MediaType mediaType = (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .headers(headers)
                .cacheControl(cacheEnable ? CacheControl.maxAge(365, TimeUnit.DAYS) : CacheControl.noCache())
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }

    private void writeContent(RandomAccessFile randomAccessFile, HttpServletResponse response, long start, long end) throws IOException {
        randomAccessFile.seek(start);
        byte[] buffer = new byte[8192];
        long bytesToRead = end - start + 1;
        int bytesRead;
        ServletOutputStream outputStream = response.getOutputStream();
        while ((bytesRead = randomAccessFile.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead))) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            bytesToRead -= bytesRead;
            if (bytesToRead <= 0) {
                break;
            }
        }
        randomAccessFile.close();
        outputStream.close();
    }

}
