package com.messageboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.messageboard.dto.MessageDTO;
import com.messageboard.dto.UserInfoDTO;
import com.messageboard.entity.Comment;
import com.messageboard.entity.Like;
import com.messageboard.entity.Message;
import com.messageboard.mapper.CommentMapper;
import com.messageboard.mapper.LikeMapper;
import com.messageboard.mapper.MessageMapper;
import com.messageboard.service.ExternalUserService;
import com.messageboard.service.MessageService;
import com.messageboard.vo.CommentVO;
import com.messageboard.vo.HotRankVO;
import com.messageboard.vo.MessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private ExternalUserService externalUserService;

    @Override
    public Page<MessageVO> pageList(Integer pageNum, Integer pageSize, String category, String currentUid) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getStatus, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Message::getCategory, category);
        }
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> page = new Page<>(pageNum, pageSize);
        Page<Message> resultPage = messageMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<MessageVO> voPage = new Page<>(pageNum, pageSize, resultPage.getTotal());
        List<MessageVO> voList = resultPage.getRecords().stream().map(msg -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(msg, vo);
            // 判断当前用户是否点赞
            if (StringUtils.hasText(currentUid)) {
                vo.setLiked(likeMapper.countByMessageIdAndUid(msg.getId(), currentUid) > 0);
            } else {
                vo.setLiked(false);
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public MessageVO getDetail(Long id, String currentUid) {
        Message message = messageMapper.selectById(id);
        if (message == null || message.getStatus() == 1) {
            throw new RuntimeException("留言不存在或已删除");
        }

        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);

        // 判断当前用户是否点赞
        if (StringUtils.hasText(currentUid)) {
            vo.setLiked(likeMapper.countByMessageIdAndUid(id, currentUid) > 0);
        } else {
            vo.setLiked(false);
        }

        // 查询评论并构建树形结构
        List<Comment> commentList = commentMapper.selectByMessageId(id);
        vo.setComments(buildCommentTree(commentList));

        return vo;
    }

    /**
     * 构建评论树形结构
     */
    private List<CommentVO> buildCommentTree(List<Comment> commentList) {
        // 转换为VO
        List<CommentVO> allVos = commentList.stream().map(c -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);
            vo.setReplies(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());

        // 按parentId分组
        Map<Long, List<CommentVO>> parentMap = allVos.stream()
                .filter(vo -> vo.getParentId() != null && vo.getParentId() > 0)
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        // 构建树：顶级评论(parentId=0)下面挂子回复
        List<CommentVO> tree = new ArrayList<>();
        for (CommentVO vo : allVos) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                // 递归挂子评论
                appendReplies(vo, parentMap);
                tree.add(vo);
            }
        }
        return tree;
    }

    private void appendReplies(CommentVO parent, Map<Long, List<CommentVO>> parentMap) {
        List<CommentVO> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setReplies(children);
            for (CommentVO child : children) {
                appendReplies(child, parentMap);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(MessageDTO dto, String uid) {
        // 查询用户信息
        UserInfoDTO userInfo = externalUserService.getUserInfo(uid);

        Message message = new Message();
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setCategory(dto.getCategory());
        message.setUid(uid);
        message.setUserName(userInfo.getUserName());
        message.setJgid(userInfo.getJgid());
        message.setOrgName(userInfo.getOrgName());
        message.setLikeCount(0);
        message.setCommentCount(0);
        message.setFilePaths(dto.getFilePaths());
        message.setFileNames(dto.getFileNames());
        message.setPhone(dto.getPhone());
        message.setStatus(0);

        messageMapper.insert(message);
        log.info("用户 {} 发布了留言：{}", uid, dto.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long messageId, String uid) {
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new RuntimeException("留言不存在或已删除");
        }

        int count = likeMapper.countByMessageIdAndUid(messageId, uid);
        if (count > 0) {
            // 已点赞 -> 取消点赞
            likeMapper.deleteByMessageIdAndUid(messageId, uid);
            message.setLikeCount(Math.max(0, message.getLikeCount() - 1));
            messageMapper.updateById(message);
            log.info("用户 {} 取消了对留言 {} 的点赞", uid, messageId);
            return false;
        } else {
            // 未点赞 -> 点赞
            Like like = new Like();
            like.setMessageId(messageId);
            like.setUid(uid);
            likeMapper.insert(like);
            message.setLikeCount(message.getLikeCount() + 1);
            messageMapper.updateById(message);
            log.info("用户 {} 对留言 {} 点赞", uid, messageId);
            return true;
        }
    }

    @Override
    public List<HotRankVO> hotByLike(Integer topN) {
        if (topN == null || topN <= 0) topN = 10;
        List<Message> list = messageMapper.selectHotByLike(topN);
        return buildHotRankVOList(list);
    }

    @Override
    public List<HotRankVO> hotByComment(Integer topN) {
        if (topN == null || topN <= 0) topN = 10;
        List<Message> list = messageMapper.selectHotByComment(topN);
        return buildHotRankVOList(list);
    }

    private List<HotRankVO> buildHotRankVOList(List<Message> list) {
        List<HotRankVO> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Message msg = list.get(i);
            HotRankVO vo = new HotRankVO();
            vo.setRank(i + 1);
            vo.setId(msg.getId());
            vo.setTitle(msg.getTitle());
            vo.setCategory(msg.getCategory());
            vo.setUserName(msg.getUserName());
            vo.setOrgName(msg.getOrgName());
            vo.setLikeCount(msg.getLikeCount());
            vo.setCommentCount(msg.getCommentCount());
            result.add(vo);
        }
        return result;
    }
}
