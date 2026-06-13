package com.digital.community.controller;

import com.digital.community.common.Result;
import com.digital.community.entity.Category;
import com.digital.community.service.CategoryService;
import com.digital.community.vo.CategoryVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> list(@RequestParam(required = false) String sort) {
        if ("hot".equals(sort)) {
            return Result.success(categoryService.listWithHotScore().stream()
                    .map(vo -> {
                        Category cat = new Category();
                        cat.setId(vo.getId());
                        cat.setName(vo.getName());
                        cat.setIcon(vo.getIcon());
                        cat.setSort(vo.getSort());
                        return cat;
                    }).toList());
        }
        return Result.success(categoryService.list());
    }

    @GetMapping("/hot")
    public Result<List<CategoryVO>> hotList() {
        return Result.success(categoryService.listWithHotScore());
    }
}
