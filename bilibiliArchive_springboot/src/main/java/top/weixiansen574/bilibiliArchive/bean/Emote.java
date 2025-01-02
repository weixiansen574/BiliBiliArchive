package top.weixiansen574.bilibiliArchive.bean;

public class Emote {
    public long id;
    public String text;
    public int size;
    public String url;
    public String fileName;

    public Emote(long id, String text, int size, String url, String fileName) {
        this.id = id;
        this.text = text;
        this.size = size;
        this.url = url;
        this.fileName = fileName;
    }

    public Emote() {
    }
}
