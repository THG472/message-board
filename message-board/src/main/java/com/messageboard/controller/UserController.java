package com.messageboard.controller;

import com.messageboard.common.Result;
import com.messageboard.common.UidUtils;
import com.messageboard.dto.UserInfoDTO;
import com.messageboard.service.ExternalUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private ExternalUserService externalUserService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result<UserInfoDTO> currentUser() {
        String uid = UidUtils.getCurrentUid();
        if (uid == null || uid.isEmpty()) {
            return Result.error(401, "未获取到用户UID");
        }
        return Result.success(externalUserService.getUserInfo(uid));
    }
}
