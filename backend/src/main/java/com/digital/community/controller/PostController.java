package com.digital.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digital.community.common.Result;
import com.digital.community.context.UserContext;
import com.digital.community.dto.PostDTO;
import com.digital.community.exception.UnauthorizedException;
import com.digital.community.service.PostService;
import com.digital.community.vo.CollocationSchemeVO;
import com.digital.community.vo.FaultThemeSuggestionVO;
import com.digital.community.vo.ModelFaultStatsVO;
import com.digital.community.vo.PostVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Resource
    private PostService postService;

    @GetMapping
    public Result<Page<PostVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "latest") String sort) {
        return Result.success(postService.page(pageNum, pageSize, categoryId, type, sort));
    }

    @GetMapping("/search")
    public Result<Page<PostVO>> search(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer type) {
        return Result.success(postService.search(pageNum, pageSize, keyword, categoryId, type));
    }

    @GetMapping("/search/suggestions")
    public Result<List<PostVO>> searchSuggestions(@RequestParam String keyword) {
        return Result.success(postService.searchSuggestions(keyword));
    }

    @GetMapping("/latest")
    public Result<List<PostVO>> latest() {
        return Result.success(postService.latestPosts());
    }

    @GetMapping("/hot")
    public Result<List<PostVO>> hot() {
        return Result.success(postService.hotPosts());
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.success(postService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody PostDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录后再发布帖子");
        }
        return Result.success(postService.create(userId, dto));
    }

    @GetMapping("/fault-suggestions")
    public Result<FaultThemeSuggestionVO> faultSuggestions(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(postService.getFaultSuggestions(model, categoryId));
    }

    @GetMapping("/fault-hot-models")
    public Result<List<ModelFaultStatsVO>> faultHotModels(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(postService.getHotFaultModels(categoryId, limit));
    }

    @GetMapping("/collocation-schemes")
    public Result<List<CollocationSchemeVO>> collocationSchemes(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "2") Integer minItems,
            @RequestParam(defaultValue = "5") Integer maxItems,
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(postService.getCollocationSchemes(categoryId, minItems, maxItems, limit));
    }
}
