package top.weixiansen574.bilibiliArchive.core.operation.progress;


import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

public final class ProgressObserver {
    private final SseEmitter sseEmitter;
    public final Integer idFilter;


    public ProgressObserver(SseEmitter sseEmitter) {
        this(sseEmitter,null);
    }

    public ProgressObserver(SseEmitter sseEmitter,Integer idFilter){
        this.sseEmitter = sseEmitter;
        this.idFilter = idFilter;
    }

    public boolean send(Progress progress) {
        try {
            if (idFilter != null){
                if (progress.id == idFilter){
                    sseEmitter.send(progress);
                    if (progress.type == PG.TYPE_CANCEL){
                        sseEmitter.complete();
                        return false;
                    }
                }
            } else {
                sseEmitter.send(progress);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public SseEmitter sseEmitter() {
        return sseEmitter;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ProgressObserver) obj;
        return Objects.equals(this.sseEmitter, that.sseEmitter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sseEmitter);
    }

    @Override
    public String toString() {
        return "ProgressObserver[" +
                "sseEmitter=" + sseEmitter + ']';
    }

    /*@ExceptionHandler(IOException.class)
    public void handlerIOException(IOException e){
        System.out.println(e);
    }*/


}
