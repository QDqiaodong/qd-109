package com.digital.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
