package com.messageboard.service;

import com.messageboard.dto.UserInfoDTO;

public interface ExternalUserService {

    /**
     * 根据UID获取用户信息（姓名、机构ID、机构名称）
     */
    UserInfoDTO getUserInfo(String uid);
}
