package com.messageboard.controller;

import com.messageboard.common.Result;
import com.messageboard.common.UidUtils;
import com.messageboard.dto.CommentDTO;
import com.messageboard.service.CommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody CommentDTO dto) {
        String uid = UidUtils.getCurrentUid();
        if (uid == null || uid.isEmpty()) {
            return Result.error(401, "请先登录，UID不能为空");
        }
        commentService.publish(dto, uid);
        return Result.success();
    }
}
