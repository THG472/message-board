package com.messageboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messageboard.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据留言ID查询评论列表
     */
    @Select("SELECT * FROM tb_comment WHERE message_id = #{messageId} AND status = 0 ORDER BY create_time ASC")
    List<Comment> selectByMessageId(@Param("messageId") Long messageId);
}
