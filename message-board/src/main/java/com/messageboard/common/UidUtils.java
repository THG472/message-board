package com.messageboard.common;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 从请求头获取当前用户UID
 */
public class UidUtils {

    private static final String UID_HEADER = "X-User-Uid";

    /**
     * 获取当前请求中的用户UID
     */
    public static String getCurrentUid() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String uid = request.getHeader(UID_HEADER);
                if (uid != null && !uid.isEmpty()) {
                    return uid;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
