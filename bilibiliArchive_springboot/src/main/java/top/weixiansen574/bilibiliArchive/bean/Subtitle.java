package top.weixiansen574.bilibiliArchive.bean;

import com.alibaba.fastjson2.JSON;
import top.weixiansen574.bilibiliArchive.core.biliApis.model.SubtitleInfo;

public class Subtitle {
    public long id;
    public String lan;//language
    public String lan_doc;
    public String content;//json
    public Subtitle(long id, String lan, String lan_doc, String content) {
        this.id = id;
        this.lan = lan;
        this.lan_doc = lan_doc;
        this.content = content;
    }

    public Subtitle(SubtitleInfo subtitle, String content) {
        this.id = subtitle.id;
        this.lan = subtitle.lan;
        this.lan_doc = subtitle.lan_doc;
        this.content = content;
    }

    public Subtitle() {
    }

    public SubtitleContent contentToObject(){
        return JSON.parseObject(content, SubtitleContent.class);
    }
}
