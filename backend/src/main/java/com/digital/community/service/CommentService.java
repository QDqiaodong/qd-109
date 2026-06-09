package com.digital.community.service;

import com.digital.community.dto.CommentDTO;
import com.digital.community.entity.Comment;
import com.digital.community.mapper.CommentMapper;
import com.digital.community.vo.CommentVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private PostService postService;

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
        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setReplyUserId(dto.getReplyUserId());
        comment.setContent(dto.getContent());
        commentMapper.insert(comment);

        postService.incrementCommentCount(dto.getPostId());

        return comment.getId();
    }
}
