package com.messageboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@Data
@TableName("tb_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人UID */
    private String uid;

    /** 操作人姓名 */
    private String userName;

    /** 操作类型：CREATE-发布留言, COMMENT-评论, LIKE-点赞, UNLIKE-取消点赞 */
    private String operationType;

    /** 操作描述 */
    private String description;

    /** 关联留言ID */
    private Long messageId;

    /** 请求IP */
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
