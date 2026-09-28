package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.entity.User;
import org.example.reggie_2402.mapper.UserMapper;
import org.example.reggie_2402.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 移动端用户业务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
