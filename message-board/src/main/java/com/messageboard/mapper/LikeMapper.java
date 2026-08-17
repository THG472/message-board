package com.messageboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messageboard.entity.Like;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LikeMapper extends BaseMapper<Like> {

    /**
     * 检查用户是否已点赞
     */
    @Select("SELECT COUNT(*) FROM tb_like WHERE message_id = #{messageId} AND uid = #{uid}")
    int countByMessageIdAndUid(@Param("messageId") Long messageId, @Param("uid") String uid);

    /**
     * 根据messageId和uid删除点赞
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM tb_like WHERE message_id = #{messageId} AND uid = #{uid}")
    int deleteByMessageIdAndUid(@Param("messageId") Long messageId, @Param("uid") String uid);
}
