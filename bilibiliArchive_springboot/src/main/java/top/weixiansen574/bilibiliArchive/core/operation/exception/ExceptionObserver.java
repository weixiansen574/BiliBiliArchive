package top.weixiansen574.bilibiliArchive.core.operation.exception;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class ExceptionObserver {
    public final SseEmitter sseEmitter;

    public ExceptionObserver(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    public boolean send(ExceptionRecord exceptionRecord){
        try {
            sseEmitter.send(exceptionRecord);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
