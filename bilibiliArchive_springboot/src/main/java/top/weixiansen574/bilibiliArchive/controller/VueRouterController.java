package top.weixiansen574.bilibiliArchive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VueRouterController {

    @RequestMapping({"/home","/user/**","/video/**","/video-backup-config/**","/settings/**","/update-plans/**","/emote-manger/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }

}