package com.digital.community.service;

import com.digital.community.dto.CommentDTO;
import com.digital.community.entity.Comment;
import com.digital.community.mapper.CommentMapper;
import com.digital.community.vo.CommentVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CommentService {

    private static final String COMMENT_SUBMIT_LOCK_PREFIX = "comment:submit:";
    private static final long COMMENT_SUBMIT_LOCK_SECONDS = 5;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private PostService postService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public List<CommentVO> list(Long postId) {
        List<CommentVO> allComments = commentMapper.selectCommentsByPostId(postId);
        return buildCommentTree(allComments);
    }

    private List<CommentVO> buildCommentTree(List<CommentVO> comments) {
        Map<Long, List<CommentVO>> childrenMap = new HashMap<>();
        List<CommentVO> roots = new ArrayList<>();

        for (CommentVO comment : comments) {
            if (comment.getParentId() == null) {
                roots.add(comment);
            } else {
                childrenMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
            }
        }

        for (CommentVO root : roots) {
            root.setChildren(childrenMap.get(root.getId()));
        }

        return roots;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, CommentDTO dto) {
        String idempotentKey = buildIdempotentKey(userId, dto);
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", COMMENT_SUBMIT_LOCK_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(absent)) {
            throw new IllegalStateException("您的操作过于频繁，请稍后再试");
        }

        try {
            if (dto.getParentId() != null) {
                Comment parentComment = commentMapper.selectById(dto.getParentId());
                if (parentComment == null || parentComment.getDeleted() == 1) {
                    throw new IllegalArgumentException("回复的评论不存在或已被删除");
                }
                if (!parentComment.getPostId().equals(dto.getPostId())) {
                    throw new IllegalArgumentException("回复的评论不属于当前帖子");
                }
            }

            Comment comment = new Comment();
            comment.setPostId(dto.getPostId());
            comment.setUserId(userId);
            comment.setParentId(dto.getParentId());
            comment.setReplyUserId(dto.getReplyUserId());
            comment.setContent(dto.getContent());
            commentMapper.insert(comment);

            postService.incrementCommentCount(dto.getPostId());

            return comment.getId();
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            throw e;
        }
    }

    private String buildIdempotentKey(Long userId, CommentDTO dto) {
        String raw = userId + ":" + dto.getPostId() + ":"
                + (dto.getParentId() == null ? "0" : dto.getParentId()) + ":"
                + (dto.getReplyUserId() == null ? "0" : dto.getReplyUserId()) + ":"
                + (dto.getContent() == null ? "" : dto.getContent());
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return COMMENT_SUBMIT_LOCK_PREFIX + sb;
        } catch (Exception e) {
            return COMMENT_SUBMIT_LOCK_PREFIX + raw.hashCode();
        }
    }
}
