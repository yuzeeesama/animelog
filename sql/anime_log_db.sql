-- 追番日志馆数据库脚本
-- 适用环境：MySQL 8.x

CREATE DATABASE IF NOT EXISTS anime_log_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE anime_log_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    bio VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0普通用户 1管理员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 番剧信息表
-- ----------------------------
DROP TABLE IF EXISTS anime;
CREATE TABLE anime (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '番剧名称',
    original_name VARCHAR(150) DEFAULT NULL COMMENT '原始名称',
    cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面地址',
    total_episodes INT NOT NULL DEFAULT 0 COMMENT '总集数',
    type VARCHAR(50) DEFAULT NULL COMMENT '类型: 热血/校园/治愈等',
    source_type VARCHAR(50) DEFAULT NULL COMMENT '来源类型: 漫画/小说/原创等',
    release_year INT DEFAULT NULL COMMENT '上映年份',
    season VARCHAR(20) DEFAULT NULL COMMENT '季度: 春/夏/秋/冬',
    synopsis TEXT COMMENT '简介',
    source_provider VARCHAR(50) DEFAULT NULL COMMENT '外部来源提供方: bangumi',
    source_subject_id BIGINT DEFAULT NULL COMMENT '外部来源条目ID',
    source_synced_at DATETIME DEFAULT NULL COMMENT '外部来源同步时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_anime_name (name),
    KEY idx_anime_type (type),
    UNIQUE KEY uk_anime_source_subject (source_provider, source_subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧信息表';

-- ----------------------------
-- 3. 标签表
-- ----------------------------
DROP TABLE IF EXISTS anime_tag;
CREATE TABLE anime_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_anime_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧标签表';

-- ----------------------------
-- 4. 番剧标签关联表
-- ----------------------------
DROP TABLE IF EXISTS anime_tag_rel;
CREATE TABLE anime_tag_rel (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    anime_id BIGINT NOT NULL COMMENT '番剧ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_anime_tag_rel (anime_id, tag_id),
    KEY idx_anime_tag_rel_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧标签关联表';

-- ----------------------------
-- 5. 用户追番关系表
-- ----------------------------
DROP TABLE IF EXISTS user_anime;
CREATE TABLE user_anime (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    anime_id BIGINT NOT NULL COMMENT '番剧ID',
    watch_status TINYINT NOT NULL DEFAULT 0 COMMENT '追番状态: 0想看 1在看 2看完 3搁置 4弃番',
    current_episode INT NOT NULL DEFAULT 0 COMMENT '当前看到第几集',
    score DECIMAL(3,1) DEFAULT NULL COMMENT '个人评分，满分10分',
    is_favorite TINYINT NOT NULL DEFAULT 0 COMMENT '是否收藏: 0否 1是',
    start_date DATE DEFAULT NULL COMMENT '开始追番日期',
    finish_date DATE DEFAULT NULL COMMENT '追番结束日期',
    last_watch_time DATETIME DEFAULT NULL COMMENT '最后观看时间',
    remark VARCHAR(255) DEFAULT NULL COMMENT '追番备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_anime (user_id, anime_id),
    KEY idx_user_anime_user_id (user_id),
    KEY idx_user_anime_anime_id (anime_id),
    KEY idx_user_anime_watch_status (watch_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户追番关系表';

-- ----------------------------
-- 6. 单集日志表
-- ----------------------------
DROP TABLE IF EXISTS episode_log;
CREATE TABLE episode_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    anime_id BIGINT NOT NULL COMMENT '番剧ID',
    user_anime_id BIGINT NOT NULL COMMENT '用户追番关系ID',
    episode_no INT NOT NULL COMMENT '集数',
    episode_title VARCHAR(100) DEFAULT NULL COMMENT '本集标题',
    content TEXT COMMENT '日志内容',
    mood_tag VARCHAR(50) DEFAULT NULL COMMENT '心情标签，如泪目/热血/治愈',
    score DECIMAL(3,1) DEFAULT NULL COMMENT '本集评分，满分10分',
    is_highlight TINYINT NOT NULL DEFAULT 0 COMMENT '是否为神回: 0否 1是',
    watched_at DATETIME DEFAULT NULL COMMENT '观看时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_episode_log_unique (user_id, anime_id, episode_no),
    KEY idx_episode_log_user_anime_id (user_anime_id),
    KEY idx_episode_log_anime_id (anime_id),
    KEY idx_episode_log_watched_at (watched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单集日志表';

-- ----------------------------
-- 7. 文件资源表
-- ----------------------------
DROP TABLE IF EXISTS file_resource;
CREATE TABLE file_resource (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT DEFAULT NULL COMMENT '上传用户ID',
    biz_type VARCHAR(50) DEFAULT NULL COMMENT '业务类型: avatar/anime_cover/log_image',
    biz_id BIGINT DEFAULT NULL COMMENT '业务ID',
    file_name VARCHAR(150) NOT NULL COMMENT '文件名',
    file_url VARCHAR(255) NOT NULL COMMENT '文件访问地址',
    file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小，单位字节',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_file_resource_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源表';

-- ----------------------------
-- 8. 初始化管理员账号
-- 默认账号: admin
-- 默认密码: 123456
-- 这里使用 BCrypt 密文，与后端 BCryptPasswordEncoder 保持一致
-- ----------------------------
INSERT INTO sys_user (username, password, nickname, role, status)
VALUES ('admin', '$2b$12$6fsa6atO080qdlkjag64puWCV76gJfaJdLC12jBSFNfkNTHK94Yra', '系统管理员', 1, 1)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    nickname = VALUES(nickname),
    role = VALUES(role),
    status = VALUES(status);

SET FOREIGN_KEY_CHECKS = 1;


