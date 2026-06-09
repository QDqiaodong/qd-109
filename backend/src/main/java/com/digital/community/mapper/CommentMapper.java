package com.digital.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.community.entity.Comment;
import com.digital.community.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    List<CommentVO> selectCommentsByPostId(@Param("postId") Long postId);
}
