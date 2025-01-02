package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import top.weixiansen574.bilibiliArchive.bean.response.BackupRCode;
import top.weixiansen574.bilibiliArchive.bean.response.BaseResponse;
import top.weixiansen574.bilibiliArchive.bean.response.ResponseCode;
import top.weixiansen574.bilibiliArchive.exceptions.RuntimeConfigurationEditException;

import java.io.IOException;

@ControllerAdvice
public class ExceptionHandlers {
    @ExceptionHandler(RuntimeConfigurationEditException.class)
    @ResponseBody
    public BaseResponse<Void> handleMyCustomException(RuntimeConfigurationEditException ex) {
        return BaseResponse.error(BackupRCode.CAN_NOT_CHANGE_CONFIG_AT_RUNNING,ex.getMessage());
    }

    /*@ExceptionHandler(IOException.class)
    public void handlerIOExceptionSse(IOException e){
        System.out.println(e);
    }*/


    @ResponseBody
    @ExceptionHandler(IOException.class)
    public BaseResponse<Void> handlerIOException(IOException e){
        return BaseResponse.error(ResponseCode.IO_EXCEPTION,e.getMessage());
    }
}
