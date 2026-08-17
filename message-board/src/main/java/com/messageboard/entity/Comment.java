package com.messageboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论表
 */
@Data
@TableName("tb_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联留言ID */
    private Long messageId;

    /** 评论内容 */
    private String content;

    /** 评论人UID */
    private String uid;

    /** 评论人姓名 */
    private String userName;

    /** 评论人机构名称 */
    private String orgName;

    /** 父评论ID，0表示顶级评论 */
    private Long parentId;

    /** 被回复人UID */
    private String replyToUid;

    /** 被回复人姓名 */
    private String replyToName;

    /** 状态：0-正常, 1-已删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
