package top.weixiansen574.bilibiliArchive.bean;

public class SubtitleContent {
    public double font_size;
    public String font_color;
    public double background_alpha;
    public String background_color;
    public String Stroke;
    public String stroke;
    public String type;
    public String lang;
    public String version;
    public Body[] body;

    public static class Body {
        public double from;
        public double to;
        public int sid;
        public int location;
        public String content;
        public double music;
    }

    public String toSRT() {
        StringBuilder srtBuilder = new StringBuilder();
        for (Body entry : body) {
            srtBuilder.append(entry.sid).append("\n");
            srtBuilder.append(formatTime(entry.from)).append(" --> ").append(formatTime(entry.to)).append("\n");
            srtBuilder.append(entry.content).append("\n\n");
        }
        return srtBuilder.toString();
    }

    // Helper method to format time from double (seconds) to SRT time format
    private String formatTime(double timeInSeconds) {
        int hours = (int) (timeInSeconds / 3600);
        int minutes = (int) ((timeInSeconds % 3600) / 60);
        int seconds = (int) (timeInSeconds % 60);
        int milliseconds = (int) ((timeInSeconds - (int) timeInSeconds) * 1000);
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
    }

}