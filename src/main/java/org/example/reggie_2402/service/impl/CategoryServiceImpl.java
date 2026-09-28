package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.common.CustomException;
import org.example.reggie_2402.entity.Category;
import org.example.reggie_2402.entity.Dish;
import org.example.reggie_2402.entity.Setmeal;
import org.example.reggie_2402.mapper.CategoryMapper;
import org.example.reggie_2402.service.CategoryService;
import org.example.reggie_2402.service.DishService;
import org.example.reggie_2402.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishService dishService;

    @Override
    @Transactional
    public void remove(Long id) {
//查询当前分类是否被菜品所关联，如果被关联则不能删除该分类，需抛出业务异常：
//select count(*) from dish where category_id = ?
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>
                ();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count = dishService.count(dishLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
//查询当前分类是否被套餐所关联，如果被关联则不能删除该分类，需抛出业务异常：
//select count(*) from setmeal where category_id = ?
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new
                LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        count = setmealService.count(setmealLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
//正常删除分类信息：delete from category where id = ?
        super.removeById(id);
    }
}
