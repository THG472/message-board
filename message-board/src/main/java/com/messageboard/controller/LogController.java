package com.messageboard.controller;

import com.messageboard.common.Result;
import com.messageboard.common.UidUtils;
import com.messageboard.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 页面访问日志接口
 */
@RestController
@RequestMapping("/api/log")
public class LogController {

    @Resource
    private OperationLogService operationLogService;

    /**
     * 记录访问主页日志（前端进入系统主页时上报一次）
     * 未带 UID（未登录）时不写入日志，保证日志表 uid 字段必有值
     */
    @PostMapping("/page-view")
    public Result<Void> pageView() {
        String uid = UidUtils.getCurrentUid();
        operationLogService.record(uid, "LOGIN", "登录访问留言板主页", null);
        return Result.success();
    }

    /**
     * 获取服务器当前时间戳（毫秒）
     * 前端用于校准页面水印时间，避免客户端本地时钟不准影响水印时间
     */
    @GetMapping("/server-time")
    public Result<Long> serverTime() {
        return Result.success(System.currentTimeMillis());
    }
}
