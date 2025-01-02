SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for avatars
-- ----------------------------
DROP TABLE IF EXISTS `avatars`;
CREATE TABLE `avatars` (
                           `name` varchar(255) NOT NULL,
                           `data` longblob,
                           PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
                            `rpid` bigint NOT NULL,
                            `oid` bigint NOT NULL,
                            `type` int NOT NULL,
                            `mid` bigint NOT NULL,
                            `root` bigint NOT NULL,
                            `parent` bigint NOT NULL,
                            `uname` varchar(255) NOT NULL,
                            `current_level` int NOT NULL,
                            `location` varchar(255) DEFAULT NULL,
                            `message` mediumtext NOT NULL,
                            `like` int NOT NULL,
                            `ctime` bigint NOT NULL,
                            `pictures` text,
                            `avatar_url` varchar(255) NOT NULL,
                            `vip_type` int NOT NULL DEFAULT '0',
                            `floor` int DEFAULT NULL,
                            `reply_count` int NOT NULL DEFAULT '0',
                            PRIMARY KEY (`rpid`),
                            KEY `avatar_url_index` (`avatar_url`),
  KEY `ctime_index` (`ctime` DESC),
  KEY `idx_oid_root` (`oid`,`root`),
  KEY `like_index` (`like` DESC),
  KEY `oid_index` (`oid`),
  KEY `root_index` (`root`),
  KEY `type_index` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;