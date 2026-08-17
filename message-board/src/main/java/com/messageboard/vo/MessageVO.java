package com.messageboard.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 留言详情返回
 */
@Data
public class MessageVO {

    private Long id;
    private String title;
    private String content;
    private String category;
    private String uid;
    private String userName;
    private String jgid;
    private String orgName;
    private Integer likeCount;
    private Integer commentCount;

    /** 当前用户是否已点赞 */
    private Boolean liked;

    /** 附件文件路径（多个用逗号分隔） */
    private String filePaths;

    /** 附件原始文件名（多个用逗号分隔） */
    private String fileNames;

    /** 手机号 */
    private String phone;

    /** 评论列表（树形结构） */
    private List<CommentVO> comments;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
