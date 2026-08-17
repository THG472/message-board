package com.messageboard.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 发布留言请求
 */
@Data
public class MessageDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /** 分类：question / requirement */
    @NotBlank(message = "分类不能为空")
    private String category;

    /** 附件文件路径（多个用逗号分隔） */
    private String filePaths;

    /** 附件原始文件名（多个用逗号分隔） */
    private String fileNames;

    /** 手机号 */
    private String phone;
}
