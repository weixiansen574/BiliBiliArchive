package top.weixiansen574.bilibiliArchive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import top.weixiansen574.bilibiliArchive.bean.VideoBackupConfig;
import top.weixiansen574.bilibiliArchive.controller.BackupController;
import top.weixiansen574.bilibiliArchive.core.biliApis.BiliBiliApiException;
import top.weixiansen574.bilibiliArchive.core.http.HttpLogger;
import top.weixiansen574.bilibiliArchive.mapper.master.BackupFavMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoBackupConfigMapper;
import top.weixiansen574.bilibiliArchive.mapper.master.VideoInfoMapper;
import top.weixiansen574.bilibiliArchive.services.BackupService;
import top.weixiansen574.bilibiliArchive.services.CommentService;
import top.weixiansen574.bilibiliArchive.services.FileService;
import top.weixiansen574.bilibiliArchive.services.UserService;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

@SpringBootApplication
//slogan --本软件：让我们的记忆永不失效
//slogan#2 --如果可以做成类似web archive的网站：限流即荣誉，补档即殉道，失效即封神;限流即荣誉，补档即传承，失效即封神
public class BilibiliArchiveApplication {

    public static void main(String[] args) throws BiliBiliApiException, IOException, InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(BilibiliArchiveApplication.class, args);
        if (args == null){
            return;
        }
        //命令行添加-start，在启动后同时启动备份
        if (args.length >= 1 && "-start".equals(args[0])){
            BackupService backupService = context.getBean(BackupService.class);
            backupService.start();
        }
    }

}
