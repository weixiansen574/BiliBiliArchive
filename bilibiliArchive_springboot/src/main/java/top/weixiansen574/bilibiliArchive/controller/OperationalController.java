package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.weixiansen574.bilibiliArchive.bean.response.BaseResponse;
import top.weixiansen574.bilibiliArchive.core.operation.exception.ExceptionObserver;
import top.weixiansen574.bilibiliArchive.core.operation.exception.ExceptionRecord;
import top.weixiansen574.bilibiliArchive.core.operation.exception.ExceptionRecorder;
import top.weixiansen574.bilibiliArchive.core.operation.progress.PG;
import top.weixiansen574.bilibiliArchive.core.operation.progress.ProgressObserver;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/operational")
public class OperationalController {

    @RequestMapping("progress")
    public SseEmitter registerProgressObserver() {
        SseEmitter sseEmitter = new SseEmitter(0L);
        PG.addObserver(new ProgressObserver(sseEmitter));
        return sseEmitter;
    }

    @RequestMapping("progress/{pid}")
    public SseEmitter registerProgressObserver(@PathVariable int pid) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        PG.addObserver(new ProgressObserver(sseEmitter));
        return sseEmitter;
    }

    @RequestMapping("exceptions")
    public SseEmitter registerExceptionObserver(){
        SseEmitter sseEmitter = new SseEmitter(0L);
        ExceptionRecorder.addObserver(new ExceptionObserver(sseEmitter));
        return sseEmitter;
    }

    @GetMapping("exceptions/all")
    public BaseResponse<List<ExceptionRecord>> getExceptions(){
        return BaseResponse.ok(ExceptionRecorder.getExceptionRecords());
    }

    @DeleteMapping("exceptions/{id}")
    public BaseResponse<Long> removeException(@PathVariable long id){
        ExceptionRecord record = ExceptionRecorder.remove(id);
        if (record != null){
            return BaseResponse.ok(record.id());
        } else {
            return BaseResponse.notfound("异常信息未找到");
        }
    }

    //sse消息直到浏览器端关闭连接为止。加个异常处理，否则断开连接后，ProgressObserver.send()方法调用将会输出一大坨异常信息到控制台，即便方法里没有打印异常信息
    @ExceptionHandler(IOException.class)
    public void handlerIOExceptionSse(IOException e){
    }

}
