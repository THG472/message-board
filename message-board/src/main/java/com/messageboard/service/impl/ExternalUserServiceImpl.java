package com.messageboard.service.impl;

import com.messageboard.dto.UserInfoDTO;
import com.messageboard.mapper.ExternalUserMapper;
import com.messageboard.service.ExternalUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ExternalUserServiceImpl implements ExternalUserService {

    @Resource
    private ExternalUserMapper externalUserMapper;

    @Override
    public UserInfoDTO getUserInfo(String uid) {
        try {
            // 查询用户姓名和机构ID
            UserInfoDTO userInfo = externalUserMapper.selectUserByUid(uid);
            if (userInfo == null) {
                log.warn("未找到用户信息，uid: {}", uid);
                UserInfoDTO fallback = new UserInfoDTO();
                fallback.setUserName("未知用户");
                fallback.setJgid("");
                fallback.setSfzh("");
                fallback.setOrgName("");
                return fallback;
            }
            // 查询机构名称
            if (userInfo.getJgid() != null && !userInfo.getJgid().isEmpty()) {
                String orgName = externalUserMapper.selectOrgNameByJgid(userInfo.getJgid());
                userInfo.setOrgName(orgName != null ? orgName : "");
            }
            return userInfo;
        } catch (Exception e) {
            log.error("查询用户信息异常，uid: {}", uid, e);
            UserInfoDTO fallback = new UserInfoDTO();
            fallback.setUserName("未知用户");
            fallback.setJgid("");
            fallback.setSfzh("");
            fallback.setOrgName("");
            return fallback;
        }
    }
}
