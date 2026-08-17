package com.messageboard.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 点赞请求
 */
@Data
public class LikeDTO {

    @NotNull(message = "留言ID不能为空")
    private Long messageId;
}
