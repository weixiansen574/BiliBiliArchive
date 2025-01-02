package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.weixiansen574.bilibiliArchive.bean.Subtitle;
import top.weixiansen574.bilibiliArchive.mapper.master.SubtitleMapper;

@RestController
@RequestMapping("/api/subtitles")
public class SubtitleController {

    public SubtitleMapper subtitleMapper;

    @Autowired
    public SubtitleController(SubtitleMapper subtitleMapper) {
        this.subtitleMapper = subtitleMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getSubtitle(@PathVariable("id") long id, @RequestParam(name = "type",defaultValue = "json") String type){
        Subtitle subtitle = subtitleMapper.selectById(id);
        if (subtitle == null){
            return ResponseEntity.notFound().build();
        }
        if (type.equals("srt")){
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/x-subrip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,  "attachment; filename=\""+id+".srt\"")
                    .body(subtitle.contentToObject().toSRT());
        } else if (type.equals("json")){
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(subtitle.content);
        }
        return ResponseEntity.badRequest().build();
    }
}
