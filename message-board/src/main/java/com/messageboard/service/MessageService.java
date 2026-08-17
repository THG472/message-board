package com.messageboard.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.messageboard.dto.MessageDTO;
import com.messageboard.vo.HotRankVO;
import com.messageboard.vo.MessageVO;

import java.util.List;

public interface MessageService {

    /**
     * 分页查询留言列表
     */
    Page<MessageVO> pageList(Integer pageNum, Integer pageSize, String category, String currentUid);

    /**
     * 获取留言详情（含评论树）
     */
    MessageVO getDetail(Long id, String currentUid);

    /**
     * 发布留言
     */
    void publish(MessageDTO dto, String uid);

    /**
     * 点赞（已点赞则取消）
     */
    boolean toggleLike(Long messageId, String uid);

    /**
     * 按点赞数热榜
     */
    List<HotRankVO> hotByLike(Integer topN);

    /**
     * 按评论数热榜
     */
    List<HotRankVO> hotByComment(Integer topN);
}
