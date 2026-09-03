package com.messageboard.service;

/**
 * 操作日志服务：统一记录用户操作日志
 * 供 AOP 切面与页面访问等场景复用
 */
public interface OperationLogService {

    /**
     * 记录一条操作日志
     *
     * @param uid           操作人UID（可为null，未登录场景）
     * @param operationType 操作类型：CREATE-发布留言, COMMENT-评论, LIKE-点赞, UNLIKE-取消点赞,
     *                      LOGIN-登录访问主页, VIEW-浏览留言
     * @param description   操作描述
     * @param messageId     关联留言ID（可为null）
     */
    void record(String uid, String operationType, String description, Long messageId);
}
