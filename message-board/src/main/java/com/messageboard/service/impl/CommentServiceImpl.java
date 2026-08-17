package com.messageboard.service.impl;

import com.messageboard.dto.CommentDTO;
import com.messageboard.dto.UserInfoDTO;
import com.messageboard.entity.Comment;
import com.messageboard.entity.Message;
import com.messageboard.mapper.CommentMapper;
import com.messageboard.mapper.MessageMapper;
import com.messageboard.service.CommentService;
import com.messageboard.service.ExternalUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private ExternalUserService externalUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(CommentDTO dto, String uid) {
        // 验证留言存在
        Message message = messageMapper.selectById(dto.getMessageId());
        if (message == null || message.getStatus() == 1) {
            throw new RuntimeException("留言不存在或已删除");
        }

        // 查询当前用户信息
        UserInfoDTO userInfo = externalUserService.getUserInfo(uid);

        Comment comment = new Comment();
        comment.setMessageId(dto.getMessageId());
        comment.setContent(dto.getContent());
        comment.setUid(uid);
        comment.setUserName(userInfo.getUserName());
        comment.setOrgName(userInfo.getOrgName());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyToUid(dto.getReplyToUid());
        comment.setStatus(0);

        // 如果是回复评论，查询被回复人信息
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            Comment parentComment = commentMapper.selectById(dto.getParentId());
            if (parentComment != null) {
                comment.setReplyToName(parentComment.getUserName());
            }
            if (dto.getReplyToUid() != null && !dto.getReplyToUid().isEmpty()) {
                UserInfoDTO replyUser = externalUserService.getUserInfo(dto.getReplyToUid());
                comment.setReplyToName(replyUser.getUserName());
            }
        }

        commentMapper.insert(comment);

        // 更新留言评论数
        message.setCommentCount((message.getCommentCount() == null ? 0 : message.getCommentCount()) + 1);
        messageMapper.updateById(message);

        log.info("用户 {} 对留言 {} 发表了评论", uid, dto.getMessageId());
    }
}
