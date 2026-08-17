package com.messageboard.aop;

import com.alibaba.fastjson.JSON;
import com.messageboard.dto.UserInfoDTO;
import com.messageboard.entity.OperationLog;
import com.messageboard.mapper.OperationLogMapper;
import com.messageboard.service.ExternalUserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志切面：记录发布留言、评论、点赞的操作日志
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private ExternalUserService externalUserService;

    /**
     * 切点：留言发布
     */
    @Pointcut("execution(* com.messageboard.service.impl.MessageServiceImpl.publish(..))")
    public void messagePublish() {}

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
     * 记录留言发布日志
     */
    @AfterReturning("messagePublish()")
    public void logMessagePublish(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 2 && args[0] != null && args[1] != null) {
                String json = JSON.toJSONString(args[0]);
                com.messageboard.dto.MessageDTO dto = JSON.parseObject(json, com.messageboard.dto.MessageDTO.class);
                String uid = (String) args[1];
                saveLog(uid, "CREATE", "发布留言：" + dto.getTitle(), null);
            }
        } catch (Exception e) {
            log.error("记录留言发布日志异常", e);
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
                saveLog(uid, "COMMENT", desc, dto.getMessageId());
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
                saveLog(uid, operationType, desc, messageId);
            }
        } catch (Exception e) {
            log.error("记录点赞日志异常", e);
        }
    }

    private void saveLog(String uid, String operationType, String description, Long messageId) {
        try {
            String userName = "未知用户";
            try {
                UserInfoDTO userInfo = externalUserService.getUserInfo(uid);
                if (userInfo != null) {
                    userName = userInfo.getUserName();
                }
            } catch (Exception ignored) {}

            String ip = "unknown";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ip = getClientIp(request);
                }
            } catch (Exception ignored) {}

            OperationLog logEntity = new OperationLog();
            logEntity.setUid(uid);
            logEntity.setUserName(userName);
            logEntity.setOperationType(operationType);
            logEntity.setDescription(description);
            logEntity.setMessageId(messageId);
            logEntity.setIp(ip);
            operationLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
