package com.messageboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 点赞表
 */
@Data
@TableName("tb_like")
public class Like {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联留言ID */
    private Long messageId;

    /** 点赞人UID */
    private String uid;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
