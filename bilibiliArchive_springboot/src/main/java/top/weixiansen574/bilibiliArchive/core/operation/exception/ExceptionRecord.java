package top.weixiansen574.bilibiliArchive.core.operation.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public record ExceptionRecord(long id,Throwable throwable, Date date,String message) {
    public long getTimestamp(){
        return date.getTime();
    }
    public String getExceptionString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
