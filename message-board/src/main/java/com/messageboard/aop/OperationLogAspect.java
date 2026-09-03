package com.messageboard.aop;

import com.alibaba.fastjson.JSON;
import com.messageboard.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 操作日志切面：记录发布留言、浏览留言、评论、点赞的操作日志
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private OperationLogService operationLogService;

    /**
     * 切点：留言发布
     */
    @Pointcut("execution(* com.messageboard.service.impl.MessageServiceImpl.publish(..))")
    public void messagePublish() {}

    /**
     * 切点：浏览留言详情
     */
    @Pointcut("execution(* com.messageboard.service.impl.MessageServiceImpl.getDetail(..))")
    public void messageView() {}

    /**
     * 切点：评论发布
     */
    @Pointcut("execution(* com.messageboard.service.impl.CommentServiceImpl.publish(..))")
    public void commentPublish() {}

    /**
     * 切点：点赞/取消点赞
     */
    @Pointcut("execution(* com.messageboard.service.impl.MessageServiceImpl.toggleLike(..))")
    public void toggleLike() {}

    /**
     * 记录留言发布日志（publish 返回新留言 ID，作为日志的 messageId）
     */
    @AfterReturning(value = "messagePublish()", returning = "result")
    public void logMessagePublish(JoinPoint joinPoint, Object result) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 2 && args[0] != null && args[1] != null) {
                String json = JSON.toJSONString(args[0]);
                com.messageboard.dto.MessageDTO dto = JSON.parseObject(json, com.messageboard.dto.MessageDTO.class);
                String uid = (String) args[1];
                Long newMessageId = (result instanceof Long) ? (Long) result : null;
                operationLogService.record(uid, "CREATE", "发布留言：" + dto.getTitle(), newMessageId);
            }
        } catch (Exception e) {
            log.error("记录留言发布日志异常", e);
        }
    }

    /**
     * 记录浏览留言日志（留言详情加载成功后记录，关联被浏览的留言 ID）
     */
    @AfterReturning("messageView()")
    public void logMessageView(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 1 && args[0] != null) {
                Long messageId = (Long) args[0];
                String uid = (args.length >= 2 && args[1] != null) ? (String) args[1] : null;
                operationLogService.record(uid, "VIEW", "浏览留言", messageId);
            }
        } catch (Exception e) {
            log.error("记录浏览留言日志异常", e);
        }
    }

    /**
     * 记录评论日志
     */
    @AfterReturning("commentPublish()")
    public void logCommentPublish(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 2 && args[0] != null && args[1] != null) {
                String json = JSON.toJSONString(args[0]);
                com.messageboard.dto.CommentDTO dto = JSON.parseObject(json, com.messageboard.dto.CommentDTO.class);
                String uid = (String) args[1];
                String desc = dto.getParentId() != null && dto.getParentId() > 0 ? "回复评论" : "发表评论";
                operationLogService.record(uid, "COMMENT", desc, dto.getMessageId());
            }
        } catch (Exception e) {
            log.error("记录评论日志异常", e);
        }
    }

    /**
     * 记录点赞日志
     */
    @AfterReturning(value = "toggleLike()", returning = "result")
    public void logToggleLike(JoinPoint joinPoint, Object result) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 2) {
                Long messageId = (Long) args[0];
                String uid = (String) args[1];
                Boolean liked = (Boolean) result;
                String operationType = liked ? "LIKE" : "UNLIKE";
                String desc = liked ? "点赞留言" : "取消点赞留言";
                operationLogService.record(uid, operationType, desc, messageId);
            }
        } catch (Exception e) {
            log.error("记录点赞日志异常", e);
        }
    }
}
