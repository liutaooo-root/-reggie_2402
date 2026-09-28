package org.example.reggie_2402.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie_2402.dto.SetmealDto;
import org.example.reggie_2402.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /** 新增套餐，同时保存套餐关联的菜品 */
    void saveWithDish(SetmealDto setmealDto);

    /** 根据 id 查询套餐信息和对应的菜品信息（修改回显用） */
    SetmealDto getByIdWithDish(Long id);

    /** 修改套餐信息，同时修改套餐关联的菜品（先删后增） */
    void updateWithDish(SetmealDto setmealDto);

    /** 批量删除套餐（启售中禁止） */
    void removeWithDish(List<Long> ids);
}
