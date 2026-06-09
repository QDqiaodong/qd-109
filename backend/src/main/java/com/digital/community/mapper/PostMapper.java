package com.digital.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digital.community.entity.Post;
import com.digital.community.vo.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    Page<PostVO> selectPostPage(Page<PostVO> page, @Param("categoryId") Long categoryId, @Param("type") Integer type);
    PostVO selectPostById(@Param("id") Long id);
}
