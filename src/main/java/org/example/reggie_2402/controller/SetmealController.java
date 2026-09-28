package org.example.reggie_2402.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.dto.SetmealDto;
import org.example.reggie_2402.entity.Category;
import org.example.reggie_2402.entity.Setmeal;
import org.example.reggie_2402.service.CategoryService;
import org.example.reggie_2402.service.SetmealService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐控制器
 */
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    private static final Logger log = LoggerFactory.getLogger(SetmealController.class);

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    /** 新增套餐：请求方式 POST，参数为 JSON（套餐信息 + setmealDishes 关联菜品数组） */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐信息分页查询：请求方式 GET，路径 /setmeal/page，参数 page、pageSize、name（可空）
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据 name 进行 like 模糊查询；按更新时间降序排序（新加的排前面）
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);
        //对象拷贝（records 除外），遍历记录填充分类名称后封装为 SetmealDto 返回
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /** 根据套餐 ID 查询套餐信息和对应的菜品信息（修改页回显） */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /** 修改套餐 */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("修改套餐：{}", setmealDto);
        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功");
    }

    /**
     * 套餐批量启售/停售：请求方式 POST，路径 /setmeal/status/{statusNum}，statusNum 1 启售、0 停售，参数 ids 支持批量
     */
    @PostMapping("/status/{statusNum}")
    public R<String> updateStatus(@PathVariable Integer statusNum, @RequestParam List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId, ids);
        updateWrapper.set(Setmeal::getStatus, statusNum);
        setmealService.update(updateWrapper);
        return R.success(statusNum == 0 ? "停售成功" : "起售成功");
    }

    /** 删除套餐（支持批量，页面传 ids 数组） */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 用户端按条件查询套餐数据：请求方式 GET，路径 /setmeal/list，参数 categoryId、status（页面传 status=1 查启售套餐）
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId,
                setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus,
                setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
