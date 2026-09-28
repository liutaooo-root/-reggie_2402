package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.common.CustomException;
import org.example.reggie_2402.dto.SetmealDto;
import org.example.reggie_2402.entity.Setmeal;
import org.example.reggie_2402.entity.SetmealDish;
import org.example.reggie_2402.mapper.SetmealMapper;
import org.example.reggie_2402.service.SetmealDishService;
import org.example.reggie_2402.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时保存套餐关联的菜品。两张表操作需要事务。
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息到套餐表 setmeal（SetmealDto 继承 Setmeal，可直接传入）
        super.save(setmealDto);
        //获取套餐 id
        Long setmealId = setmealDto.getId();
        //套餐关联的菜品：页面提交的数据没有 setmeal_id，补充 setmealId 后批量保存到套餐菜品关系表 setmeal_dish
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes = setmealDishes.stream().map(item -> {
                item.setSetmealId(setmealId);
                return item;
            }).collect(Collectors.toList());
            setmealDishService.saveBatch(setmealDishes);
        }
    }

    /**
     * 根据 id 查询套餐信息和对应的菜品信息（修改页回显）
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //套餐的基本信息：select * from setmeal where id = ?
        Setmeal setmeal = super.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        //套餐关联的菜品信息：select * from setmeal_dish where setmeal_id = ?
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }

    /**
     * 修改套餐信息，同时修改套餐关联的菜品（先删后增）。两张表操作需要事务。
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新套餐表基本信息（setmeal_dish 中的 setmeal_id 不会变，所以直接 updateById）
        super.updateById(setmealDto);
        //先删除套餐关联的旧菜品数据：delete from setmeal_dish where setmeal_id = ?
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //再重新插入页面提交的套餐菜品关联数据（先删后增，统一处理关联菜品的新增/修改/删除三种情况）
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes = setmealDishes.stream().map(item -> {
                item.setSetmealId(setmealDto.getId());
                return item;
            }).collect(Collectors.toList());
            setmealDishService.saveBatch(setmealDishes);
        }
    }

    /**
     * 批量删除套餐（含关联菜品）。启售中禁止删除，抛业务异常由全局异常处理器返回提示。
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //先判断套餐是否为启售状态，启售中的套餐不允许删除：
        //select count(*) from setmeal where id in (...) and status = 1
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids).eq(Setmeal::getStatus, 1);
        int count = super.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("有启售中的套餐，不能删除！");
        }
        //删除套餐表中的套餐：delete from setmeal where id in (...)
        super.removeByIds(ids);
        //删除套餐菜品关系表中的关联数据：delete from setmeal_dish where setmeal_id in (...)
        LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishQueryWrapper);
    }
}
