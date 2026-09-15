package com.messageboard.dto;

import lombok.Data;

/**
 * 从外部库查询的用户信息
 */
@Data
public class UserInfoDTO {

    private String userName;
    private String jgid;
    /** 身份证号（来自syngbs.manex.sfzh，用于页面水印） */
    private String sfzh;
    /** 机构名称（二次查询填充） */
    private String orgName;
}
