package com.digital.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digital.community.entity.Post;
import com.digital.community.vo.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    Page<PostVO> selectPostPage(Page<PostVO> page, @Param("categoryId") Long categoryId, @Param("type") Integer type, @Param("keyword") String keyword, @Param("sort") String sort);
    PostVO selectPostById(@Param("id") Long id);
    List<PostVO> selectPostSuggestions(@Param("keyword") String keyword);

    int incrementViewCount(@Param("id") Long id);

    int incrementCommentCount(@Param("id") Long id);

    int countCommentsByPostId(@Param("postId") Long postId);

    List<PostVO> selectHelpPostsByModelKeyword(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    List<PostVO> selectAllHelpPosts(@Param("categoryId") Long categoryId);
}
