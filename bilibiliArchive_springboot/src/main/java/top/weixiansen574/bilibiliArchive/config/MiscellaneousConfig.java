package top.weixiansen574.bilibiliArchive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.weixiansen574.bilibiliArchive.core.http.HttpLog;
import top.weixiansen574.bilibiliArchive.core.http.HttpLogger;
import top.weixiansen574.bilibiliArchive.core.http.LoggingAndRetryInterceptor;

import java.io.File;

@Configuration
public class MiscellaneousConfig {
    @Bean
    public LoggingAndRetryInterceptor loggingAndRetryInterceptor
            (@Value("${files.logs}") File logDirectory,@Value("${http-req.logging}") boolean logging,
             @Value("${http-req.max-retries}") int maxRetries){
        HttpLogger httpLogger = null;
        if (logging){
            httpLogger = new HttpLogger(logDirectory);
        }
        return new LoggingAndRetryInterceptor(httpLogger,maxRetries);
    }
}
