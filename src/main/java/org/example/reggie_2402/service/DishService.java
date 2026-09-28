package org.example.reggie_2402.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie_2402.dto.DishDto;
import org.example.reggie_2402.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    // DishService 接口中新增声明：
    /** 新增菜品，同时保存对应的口味数据（两张表操作，需要事务） */
    void saveWithFlavor(DishDto dishDto);

    // DishService 接口新增三个方法声明（本实训全部方法）：
/** 根据id查询菜品信息和对应的口味信息（修改回显用） */
    DishDto getByIdWithFlavor(Long id);
    /** 修改菜品信息，同时修改对应的口味信息 */
    void updateWithFlavor(DishDto dishDto);
    /** 批量删除菜品（含口味） */
    void delByIdWithFlavor(List<Long> ids);
}
