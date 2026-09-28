package org.example.reggie_2402.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie_2402.entity.User;

/**
 * 移动端用户数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
