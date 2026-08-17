package com.messageboard.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论返回
 */
@Data
public class CommentVO {

    private Long id;
    private Long messageId;
    private String content;
    private String uid;
    private String userName;
    private String orgName;
    private Long parentId;
    private String replyToUid;
    private String replyToName;

    /** 子回复列表 */
    private List<CommentVO> replies;

    private LocalDateTime createTime;
}
