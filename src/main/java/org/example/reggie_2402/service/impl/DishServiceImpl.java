package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.common.CustomException;
import org.example.reggie_2402.dto.DishDto;
import org.example.reggie_2402.entity.Dish;
import org.example.reggie_2402.entity.DishFlavor;
import org.example.reggie_2402.mapper.DishFlavorMapper;
import org.example.reggie_2402.mapper.DishMapper;
import org.example.reggie_2402.service.DishFlavorService;
import org.example.reggie_2402.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper , Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto){
        //保存菜品的基本信息到菜品表 dish（DishDto 继承 Dish，可直接传入，插入后 dishDto 中自动回填了雪花算法生成的 id）
        super.save(dishDto);
//获取菜品id
        Long dishId = dishDto.getId();
//菜品口味，页面提交的 flavors 没有 dish_id，需要回填后批量保存到口味表 dish_flavor
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors = flavors.stream().map(item -> {
                item.setDishId(dishId);
                return item;
            }).collect(Collectors.toList());
            dishFlavorService.saveBatch(flavors);
        }
    }

    /**
     * 根据id查询菜品信息和对应的口味信息：先查菜品表拷贝为 DishDto，再查口味表填充 flavors
     */
    @Override
    public DishDto getByIdWithFlavor(Long id){
        //菜品的基本信息：select * from dish where id = ?
        Dish dish=super.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        //当前菜品对应的口味数据：select * from dish_flavor where dish_id = ?
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors=dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 修改菜品信息，同时修改对应的口味信息。两张表操作需要事务。
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
//更新 dish 表基本信息（口味表的 dish_id 不会变，所以直接 updateById）
        super.updateById(dishDto);
//清理当前菜品对应口味表中的旧数据：delete from dish_flavor where dish_id = ?
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
//将当前页面提交的口味数据重新插入到口味表（先删后增，统一处理口味的新增/修改/删除三种情况）
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors = flavors.stream().map(item -> {
                item.setDishId(dishDto.getId());
                return item;
            }).collect(Collectors.toList());
            dishFlavorService.saveBatch(flavors);
        }
    }
    /**
     * 批量删除菜品（含口味）。启售中禁止删除，抛业务异常由全局异常处理器返回提示。
     */
    @Override
    @Transactional
    public void delByIdWithFlavor(List<Long> ids){
        //先判断菜品是否为启售状态，如果是启售状态则不能删除，抛出业务异常：
//select count(*) from dish where id in (...) and status = 1
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids).eq(Dish::getStatus, 1);
        int count = super.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("有启售中的菜品，不能删除！");
        }
        //删除菜品表中的菜品：delete from dish where id in (...)
        super.removeByIds(ids);
        //删除口味表中的口味数据：delete from dish_flavor where dish_id in (...)
        LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
        flavorQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(flavorQueryWrapper);
    }
}
