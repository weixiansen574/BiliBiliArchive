SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for backups_fav
-- ----------------------------
DROP TABLE IF EXISTS `backups_fav`;
CREATE TABLE `backups_fav`
(
    `fav_id`                 bigint       NOT NULL,
    `fav_name`               varchar(255) NOT NULL,
    `owner_uid`              bigint       NOT NULL,
    `owner_name`             varchar(255) NOT NULL,
    `backup_user_id`         bigint       NOT NULL,
    `video_backup_enable`    int          NOT NULL,
    `video_backup_config_id` int          NOT NULL,
    PRIMARY KEY (`fav_id`),
    KEY                      `backup_user_id`(`backup_user_id`
) ,
  KEY `video_backup_config_id` (`video_backup_config_id`),
  CONSTRAINT `backups_fav_ibfk_1` FOREIGN KEY (`backup_user_id`) REFERENCES `users` (`uid`),
  CONSTRAINT `backups_fav_ibfk_2` FOREIGN KEY (`video_backup_config_id`) REFERENCES `video_backup_configs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for backups_history
-- ----------------------------
DROP TABLE IF EXISTS `backups_history`;
CREATE TABLE `backups_history`
(
    `uid`                       bigint NOT NULL,
    `auto_delete_method`        varchar(255) DEFAULT NULL,
    `delete_by_days`            bigint       DEFAULT NULL,
    `delete_by_disk_usage`      double       DEFAULT NULL,
    `delete_by_disk_usage_unit` varchar(10)  DEFAULT NULL,
    `delete_by_item_quantity`   int          DEFAULT NULL,
    `release_time_limit_day`    bigint       DEFAULT NULL,
    `uploader_black_list`       mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `video_backup_enable`       int    NOT NULL,
    `video_backup_config_id`    int    NOT NULL,
    PRIMARY KEY (`uid`),
    KEY                         `video_backup_config_id`(`video_backup_config_id`
) ,
  CONSTRAINT `backups_history_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `users` (`uid`),
  CONSTRAINT `backups_history_ibfk_2` FOREIGN KEY (`video_backup_config_id`) REFERENCES `video_backup_configs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for backups_uploader
-- ----------------------------
DROP TABLE IF EXISTS `backups_uploader`;
CREATE TABLE `backups_uploader`
(
    `uid`                    bigint       NOT NULL,
    `name`                   varchar(255) NOT NULL,
    `desc`                   text,
    `avatar_url`             varchar(255) NOT NULL,
    `backup_start_time`      bigint DEFAULT NULL,
    `backup_user_id`         bigint       NOT NULL,
    `video_backup_enable`    int          NOT NULL,
    `video_backup_config_id` int          NOT NULL,
    `dynamic_backup_enable`  int          NOT NULL,
    `dynamic_backup_config`  text,
    `article_backup_enable`  int          NOT NULL,
    `article_backup_config`  text,
    `live_recording_enable`  int          NOT NULL,
    `live_recording_config`  text,
    PRIMARY KEY (`uid`),
    KEY                      `backup_user_id`(`backup_user_id`
) ,
  KEY `video_backup_config_id` (`video_backup_config_id`),
  CONSTRAINT `backups_uploader_ibfk_1` FOREIGN KEY (`backup_user_id`) REFERENCES `users` (`uid`),
  CONSTRAINT `backups_uploader_ibfk_2` FOREIGN KEY (`video_backup_config_id`) REFERENCES `video_backup_configs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for emote
-- ----------------------------
DROP TABLE IF EXISTS `emote`;
CREATE TABLE `emote`
(
    `id`        int          NOT NULL,
    `text`      varchar(255) NOT NULL,
    `size`      int          NOT NULL,
    `url`       varchar(255) NOT NULL,
    `file_name` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for subtitles
-- ----------------------------
DROP TABLE IF EXISTS `subtitles`;
CREATE TABLE `subtitles`
(
    `id`      bigint NOT NULL AUTO_INCREMENT,
    `lan`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `lan_doc` varchar(255) DEFAULT NULL,
    `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    `uid`        bigint                                                      NOT NULL,
    `name`       varchar(255)                                                NOT NULL,
    `avatar_url` varchar(255)                                                NOT NULL,
    `cookie`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci       NOT NULL,
    `pfs`        mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for video_backup_configs
-- ----------------------------
DROP TABLE IF EXISTS `video_backup_configs`;
CREATE TABLE `video_backup_configs`
(
    `id`      int          NOT NULL AUTO_INCREMENT,
    `name`    varchar(255) NOT NULL,
    `video`   text,
    `comment` text,
    `update`  mediumtext,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for video_infos
-- ----------------------------
DROP TABLE IF EXISTS `video_infos`;
CREATE TABLE `video_infos`
(
    `bvid`                  varchar(255)                                          NOT NULL,
    `avid`                  bigint                                                NOT NULL,
    `title`                 text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `desc`                  text,
    `duration`              int                                                   NOT NULL DEFAULT '0',
    `owner_mid`             bigint                                                NOT NULL,
    `owner_name`            varchar(255)                                          NOT NULL,
    `owner_avatar_url`      varchar(255)                                                   DEFAULT NULL,
    `view`                  bigint                                                NOT NULL,
    `danmaku`               bigint                                                NOT NULL,
    `favorite`              bigint                                                NOT NULL,
    `coin`                  bigint                                                NOT NULL,
    `like`                  bigint                                                NOT NULL,
    `share`                 bigint                                                NOT NULL,
    `reply`                 bigint                                                NOT NULL,
    `tname`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `ctime`                 bigint                                                NOT NULL,
    `cover_url`             varchar(255)                                                   DEFAULT NULL,
    `tags`                  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `pages_version_list`    mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `state`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `downloading`           int                                                   NOT NULL,
    `save_time`             bigint                                                NOT NULL DEFAULT '0',
    `community_update_time` bigint                                                NOT NULL DEFAULT '0',
    `config_id`             int                                                   NOT NULL DEFAULT '0',
    `total_comment_floor`   int                                                   NOT NULL DEFAULT '0',
    PRIMARY KEY (`bvid`),
    UNIQUE KEY `bvid` (`bvid`),
    UNIQUE KEY `avid` (`avid`),
    KEY                     `config_id`(`config_id`
) ,
  CONSTRAINT `video_infos_ibfk_1` FOREIGN KEY (`config_id`) REFERENCES `video_backup_configs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for video_update_plans
-- ----------------------------
DROP TABLE IF EXISTS `video_update_plans`;
CREATE TABLE `video_update_plans`
(
    `id`                       int          NOT NULL AUTO_INCREMENT,
    `timestamp`                bigint       NOT NULL,
    `uid`                      bigint       NOT NULL,
    `bvid`                     varchar(255) NOT NULL,
    `avid`                     bigint       NOT NULL,
    `title`                    text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `update_video_and_danmaku` int          NOT NULL,
    `video_quality`            int DEFAULT NULL,
    `video_codec_id`           int DEFAULT NULL,
    `comment_config_1`         text,
    `comment_config_2`         text,
    `comment_config_3`         text,
    PRIMARY KEY (`id`),
    KEY                        `uid`(`uid`
) ,
  CONSTRAINT `video_update_plans_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `users` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for videos_favorite
-- ----------------------------
DROP TABLE IF EXISTS `videos_favorite`;
CREATE TABLE `videos_favorite`
(
    `fav_id`   bigint       NOT NULL,
    `bvid`     varchar(255) NOT NULL,
    `avid`     bigint       NOT NULL,
    `fav_time` bigint       NOT NULL,
    `fav_ban`  int          NOT NULL DEFAULT '0',
    PRIMARY KEY (`fav_id`, `bvid`),
    KEY        `bvid`(`bvid`
) ,
  KEY `fav_id_index` (`fav_id`),
  CONSTRAINT `videos_favorite_ibfk_1` FOREIGN KEY (`fav_id`) REFERENCES `backups_fav` (`fav_id`),
  CONSTRAINT `videos_favorite_ibfk_2` FOREIGN KEY (`bvid`) REFERENCES `video_infos` (`bvid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for videos_history
-- ----------------------------
DROP TABLE IF EXISTS `videos_history`;
CREATE TABLE `videos_history`
(
    `uid`     bigint       NOT NULL,
    `bvid`    varchar(255) NOT NULL,
    `avid`    bigint       NOT NULL,
    `view_at` bigint       NOT NULL,
    PRIMARY KEY (`uid`, `bvid`),
    KEY       `bvid`(`bvid`
) ,
  KEY `uhv_uid_index` (`uid`),
  CONSTRAINT `videos_history_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `users` (`uid`),
  CONSTRAINT `videos_history_ibfk_2` FOREIGN KEY (`bvid`) REFERENCES `video_infos` (`bvid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for videos_uploader
-- ----------------------------
DROP TABLE IF EXISTS `videos_uploader`;
CREATE TABLE `videos_uploader`
(
    `uid`     bigint       NOT NULL,
    `bvid`    varchar(255) NOT NULL,
    `avid`    bigint       NOT NULL,
    `created` bigint       NOT NULL,
    PRIMARY KEY (`uid`, `bvid`),
    KEY       `bvid`(`bvid`
) ,
  CONSTRAINT `videos_uploader_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `backups_uploader` (`uid`),
  CONSTRAINT `videos_uploader_ibfk_2` FOREIGN KEY (`bvid`) REFERENCES `video_infos` (`bvid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO video_backup_configs (id, name, video, comment, `update`) VALUES (0,'FINAL',null,null,null);
update video_backup_configs set id = 0 where name = 'FINAL';