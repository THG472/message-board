package com.messageboard.mapper;

import com.messageboard.dto.UserInfoDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 查询外部库 syngbs 中的用户和组织信息
 */
public interface ExternalUserMapper {

    /**
     * 根据UID从syngbs.manex表查询用户姓名、机构ID和身份证号（sfzh 用于页面水印）
     */
    @Select("SELECT xm AS userName, jgid, sfzh FROM syngbs.manex WHERE id = #{uid} LIMIT 1")
    UserInfoDTO selectUserByUid(@Param("uid") String uid);

    /**
     * 根据机构ID从syngbs.organ表查询机构名称
     */
    @Select("SELECT jgmc FROM syngbs.organ WHERE id = #{jgid} LIMIT 1")
    String selectOrgNameByJgid(@Param("jgid") String jgid);
}
