package top.weixiansen574.bilibiliArchive.core.operation.progress;

public final class Progress {
    public final int id;
    public int type;
    public String title;
    public String content;
    public Object data;

    public Progress(int id, int type, String title, String content, Object data) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.data = data;
    }

    public Progress(int id) {
        this.id = id;
    }
}
