package com.messageboard.service.impl;

import com.messageboard.dto.UserInfoDTO;
import com.messageboard.entity.OperationLog;
import com.messageboard.mapper.OperationLogMapper;
import com.messageboard.service.ExternalUserService;
import com.messageboard.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志服务实现：查询用户姓名/IP 后写入 tb_operation_log
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private ExternalUserService externalUserService;

    @Override
    public void record(String uid, String operationType, String description, Long messageId) {
        // 日志表 uid 字段为 NOT NULL，uid 为空时不记录，保证每条日志都有 uid
        if (uid == null || uid.isEmpty()) {
            log.warn("跳过操作日志记录: uid 为空, type: {}, desc: {}", operationType, description);
            return;
        }
        try {
            String userName = "未知用户";
            if (uid != null && !uid.isEmpty()) {
                try {
                    UserInfoDTO userInfo = externalUserService.getUserInfo(uid);
                    if (userInfo != null && userInfo.getUserName() != null) {
                        userName = userInfo.getUserName();
                    }
                } catch (Exception ignored) {}
            }

            String ip = "unknown";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    ip = getClientIp(attributes.getRequest());
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
            log.error("保存操作日志失败, type: {}, desc: {}", operationType, description, e);
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
