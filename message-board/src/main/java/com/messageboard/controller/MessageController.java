package com.messageboard.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.messageboard.common.Result;
import com.messageboard.common.UidUtils;
import com.messageboard.dto.LikeDTO;
import com.messageboard.dto.MessageDTO;
import com.messageboard.service.MessageService;
import com.messageboard.vo.HotRankVO;
import com.messageboard.vo.MessageVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /**
     * 分页查询留言列表
     */
    @GetMapping("/list")
    public Result<Page<MessageVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category) {
        String uid = UidUtils.getCurrentUid();
        return Result.success(messageService.pageList(pageNum, pageSize, category, uid));
    }

    /**
     * 获取留言详情（含评论树）
     */
    @GetMapping("/detail/{id}")
    public Result<MessageVO> detail(@PathVariable Long id) {
        String uid = UidUtils.getCurrentUid();
        return Result.success(messageService.getDetail(id, uid));
    }

    /**
     * 发布留言
     */
    @PostMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody MessageDTO dto) {
        String uid = UidUtils.getCurrentUid();
        if (uid == null || uid.isEmpty()) {
            return Result.error(401, "请先登录，UID不能为空");
        }
        messageService.publish(dto, uid);
        return Result.success();
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like")
    public Result<Boolean> like(@Valid @RequestBody LikeDTO dto) {
        String uid = UidUtils.getCurrentUid();
        if (uid == null || uid.isEmpty()) {
            return Result.error(401, "请先登录，UID不能为空");
        }
        boolean liked = messageService.toggleLike(dto.getMessageId(), uid);
        return Result.success(liked);
    }

    /**
     * 按点赞数热榜
     */
    @GetMapping("/hot/like")
    public Result<List<HotRankVO>> hotByLike(@RequestParam(defaultValue = "10") Integer topN) {
        return Result.success(messageService.hotByLike(topN));
    }

    /**
     * 按评论数热榜
     */
    @GetMapping("/hot/comment")
    public Result<List<HotRankVO>> hotByComment(@RequestParam(defaultValue = "10") Integer topN) {
        return Result.success(messageService.hotByComment(topN));
    }
}
