package com.messageboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.messageboard.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 按点赞数排行（热榜）
     */
    @Select("SELECT * FROM tb_message WHERE status = 0 ORDER BY like_count DESC LIMIT #{limit}")
    List<Message> selectHotByLike(@Param("limit") Integer limit);

    /**
     * 按评论数排行（热榜）
     */
    @Select("SELECT * FROM tb_message WHERE status = 0 ORDER BY comment_count DESC LIMIT #{limit}")
    List<Message> selectHotByComment(@Param("limit") Integer limit);
}
