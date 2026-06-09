package com.digital.community.controller;

import com.digital.community.common.Result;
import com.digital.community.dto.CommentDTO;
import com.digital.community.service.CommentService;
import com.digital.community.vo.CommentVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Resource
    private CommentService commentService;

    @GetMapping
    public Result<List<CommentVO>> list(@RequestParam Long postId) {
        return Result.success(commentService.list(postId));
    }

    @PostMapping
    public Result<Long> create(@RequestHeader("X-User-Id") Long userId, @RequestBody CommentDTO dto) {
        return Result.success(commentService.create(userId, dto));
    }
}
