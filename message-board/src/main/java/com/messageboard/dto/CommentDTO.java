package com.messageboard.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发布评论请求
 */
@Data
public class CommentDTO {

    @NotNull(message = "留言ID不能为空")
    private Long messageId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    /** 父评论ID，回复评论时使用，顶级评论传null */
    private Long parentId;

    /** 被回复人UID */
    private String replyToUid;
}
