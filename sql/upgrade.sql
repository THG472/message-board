-- =====================================================
-- 留言板系统 - 增量升级SQL（v1.1.0）
-- 新增：附件上传、手机号字段
-- =====================================================

USE message_board;

-- tb_message 表新增字段
ALTER TABLE tb_message
  ADD COLUMN file_paths TEXT DEFAULT NULL COMMENT '附件文件路径（多个逗号分隔）' AFTER comment_count,
  ADD COLUMN file_names TEXT DEFAULT NULL COMMENT '附件原始文件名（多个逗号分隔）' AFTER file_paths,
  ADD COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号' AFTER file_names;
