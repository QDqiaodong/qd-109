package com.digital.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digital.community.entity.User;
import com.digital.community.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public User login(String username, String password) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getPassword, password)
                        .eq(User::getDeleted, 0)
        );
    }

    public User register(String username, String password, String nickname) {
        User exists = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getDeleted, 0)
        );
        if (exists != null) {
            return null;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
        userMapper.insert(user);
        return user;
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
