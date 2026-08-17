-- =====================================================
-- 留言板系统 - 数据库设计SQL
-- 数据库名: message_board
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS message_board
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE message_board;

-- =====================================================
-- 1. 留言表（帖子表）
-- =====================================================
DROP TABLE IF EXISTS tb_message;
CREATE TABLE tb_message (
  id              BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  title           VARCHAR(200) NOT NULL                 COMMENT '标题',
  content         TEXT         NOT NULL                 COMMENT '内容',
  category        VARCHAR(20)  NOT NULL                 COMMENT '分类: question-问题, requirement-需求',
  uid             VARCHAR(50)  NOT NULL                 COMMENT '发布人UID（来自syngbs.manex.id）',
  user_name       VARCHAR(100) DEFAULT NULL             COMMENT '发布人姓名（查询自syngbs.manex.xm）',
  jgid            VARCHAR(50)  DEFAULT NULL             COMMENT '发布人机构ID（查询自syngbs.manex.jgid）',
  org_name        VARCHAR(200) DEFAULT NULL             COMMENT '发布人机构名称（查询自syngbs.organ.jgmc）',
  like_count      INT(11)      NOT NULL DEFAULT 0       COMMENT '点赞数',
  comment_count   INT(11)      NOT NULL DEFAULT 0       COMMENT '评论数',
  file_paths      TEXT         DEFAULT NULL             COMMENT '附件文件路径（多个逗号分隔）',
  file_names      TEXT         DEFAULT NULL             COMMENT '附件原始文件名（多个逗号分隔）',
  phone           VARCHAR(20)  DEFAULT NULL             COMMENT '手机号',
  status          TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '状态: 0-正常, 1-已删除',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX idx_category (category),
  INDEX idx_uid (uid),
  INDEX idx_like_count (like_count),
  INDEX idx_comment_count (comment_count),
  INDEX idx_create_time (create_time),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言表';


-- =====================================================
-- 2. 评论表
-- =====================================================
DROP TABLE IF EXISTS tb_comment;
CREATE TABLE tb_comment (
  id              BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  message_id      BIGINT(20)   NOT NULL                 COMMENT '关联留言ID',
  content         TEXT         NOT NULL                 COMMENT '评论内容',
  uid             VARCHAR(50)  NOT NULL                 COMMENT '评论人UID',
  user_name       VARCHAR(100) DEFAULT NULL             COMMENT '评论人姓名',
  org_name        VARCHAR(200) DEFAULT NULL             COMMENT '评论人机构名称',
  parent_id       BIGINT(20)   NOT NULL DEFAULT 0       COMMENT '父评论ID, 0表示顶级评论',
  reply_to_uid    VARCHAR(50)  DEFAULT NULL             COMMENT '被回复人UID',
  reply_to_name   VARCHAR(100) DEFAULT NULL             COMMENT '被回复人姓名',
  status          TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '状态: 0-正常, 1-已删除',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  INDEX idx_message_id (message_id),
  INDEX idx_uid (uid),
  INDEX idx_parent_id (parent_id),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';


-- =====================================================
-- 3. 点赞表
-- =====================================================
DROP TABLE IF EXISTS tb_like;
CREATE TABLE tb_like (
  id              BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  message_id      BIGINT(20)   NOT NULL                 COMMENT '关联留言ID',
  uid             VARCHAR(50)  NOT NULL                 COMMENT '点赞人UID',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_message_uid (message_id, uid),
  INDEX idx_message_id (message_id),
  INDEX idx_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';


-- =====================================================
-- 4. 操作日志表
-- =====================================================
DROP TABLE IF EXISTS tb_operation_log;
CREATE TABLE tb_operation_log (
  id              BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  uid             VARCHAR(50)  NOT NULL                 COMMENT '操作人UID',
  user_name       VARCHAR(100) DEFAULT NULL             COMMENT '操作人姓名',
  operation_type  VARCHAR(20)  NOT NULL                 COMMENT '操作类型: CREATE-发布留言, COMMENT-评论, LIKE-点赞, UNLIKE-取消点赞',
  description     VARCHAR(500) DEFAULT NULL             COMMENT '操作描述',
  message_id      BIGINT(20)   DEFAULT NULL             COMMENT '关联留言ID',
  ip              VARCHAR(50)  DEFAULT NULL             COMMENT '请求IP',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  INDEX idx_uid (uid),
  INDEX idx_operation_type (operation_type),
  INDEX idx_create_time (create_time),
  INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
