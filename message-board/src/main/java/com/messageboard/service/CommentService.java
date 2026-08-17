package com.messageboard.service;

import com.messageboard.dto.CommentDTO;

public interface CommentService {

    /**
     * 发表评论
     */
    void publish(CommentDTO dto, String uid);
}
