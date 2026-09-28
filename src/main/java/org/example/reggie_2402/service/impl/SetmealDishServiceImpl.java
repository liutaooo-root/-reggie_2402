package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.entity.SetmealDish;
import org.example.reggie_2402.mapper.SetmealDishMapper;
import org.example.reggie_2402.service.SetmealDishService;
import org.springframework.stereotype.Service;

/**
 * 套餐菜品关系业务实现
 */
@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish>
        implements SetmealDishService {
}
