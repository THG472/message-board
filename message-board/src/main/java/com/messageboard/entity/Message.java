package com.messageboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 留言/帖子表
 */
@Data
@TableName("tb_message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 分类：question-问题, requirement-需求 */
    private String category;

    /** 发布人UID */
    private String uid;

    /** 发布人姓名（从syngbs.manex查询） */
    private String userName;

    /** 发布人机构ID */
    private String jgid;

    /** 发布人机构名称（从syngbs.organ查询） */
    private String orgName;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 附件文件路径（多个用逗号分隔） */
    private String filePaths;

    /** 附件原始文件名（多个用逗号分隔） */
    private String fileNames;

    /** 手机号 */
    private String phone;

    /** 状态：0-正常, 1-已删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
