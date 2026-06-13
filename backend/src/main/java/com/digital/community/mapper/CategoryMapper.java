package com.digital.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.community.entity.Category;
import com.digital.community.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    List<CategoryVO> selectCategoryWithHotScore();
}
