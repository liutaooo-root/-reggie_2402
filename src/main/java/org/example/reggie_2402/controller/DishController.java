package org.example.reggie_2402.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.dto.DishDto;
import org.example.reggie_2402.entity.Category;
import org.example.reggie_2402.entity.Dish;
import org.example.reggie_2402.entity.DishFlavor;
import org.example.reggie_2402.service.CategoryService;
import org.example.reggie_2402.service.DishFlavorService;
import org.example.reggie_2402.service.DishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {
    private static final Logger log = LoggerFactory.getLogger(DishController.class);
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品：请求方式 POST，参数为 JSON（菜品基本信息 + flavors 口味数组）
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }
    /**
     * 菜品信息分页查询：请求方式 GET，路径 /dish/page，参数 page、pageSize、name（可空，模糊
     搜索）
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页对象（dish 表的分页结果）与 DTO 分页对象（最终返回页面）
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //构造条件构造器：添加过滤条件（name 不为空时按名称模糊查询）与排序条件（按更新时间降序）
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询：select * from dish where name like ? order by update_time desclimit ?,?
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝：把 pageInfo 的分页属性（总条数、页码等）拷到 dishDtoPage，records 属性除外（类型不同，需单独处理）
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list=records.stream().map(item->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //根据分类 id 查询分类对象，填充分类名称到 dishDto（页面列表展示“分类名称”列需要）
            Category category=categoryService.getById(item.getCategoryId());
            if (category!=null){
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /** 根据id查询菜品信息和对应的口味信息（修改页回显） */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto=dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /** 修改菜品 */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 菜品批量启售/停售：请求方式 POST，路径 /dish/status/{statusNum}，statusNum 1 启售、0
     停售，参数 ids 支持批量（页面传数组）
     */
    @PostMapping("/status/{statusNum}")
    public R<String> updateStatus(@PathVariable Integer statusNum, @RequestParam List<Long> ids){
        log.info("批量启售/停售：{}，{}",statusNum,ids);
        //构造条件构造器：update dish set status = ? where id in (...)
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids);
        updateWrapper.set(Dish::getStatus, statusNum);
        dishService.update(updateWrapper);
        return R.success(statusNum == 0 ? "停售成功" : "起售成功");
    }

    /** 删除菜品（支持批量，页面传 ids 数组） */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品：{}", ids);
        dishService.delByIdWithFlavor(ids);
        return R.success("删除成功");
    }

    /**
     * 用户端按分类查询启售菜品（含分类名称与口味）：请求方式 GET，路径 /dish/list，参数 categoryId
     * 说明：管理端“添加菜品”对话框与用户端首页菜品展示共用此接口，返回 List<DishDto>
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //categoryId 不为空时才加该条件（分类下拉切换时传）
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId,
                dish.getCategoryId());
        //只查询状态为 1（起售状态）的菜品，用户不能点到停售的菜；按 sort 升序、更新时间降序排序
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> dishDtoList = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            //填充分类名称：根据分类 id 查询分类对象，取分类名设置到 dishDto 的 categoryName 属性
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            //查询菜品口味：select * from dish_flavor where dish_id = ?，设置到 dishDto 的 flavors 集合
            LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
            flavorQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavorList = dishFlavorService.list(flavorQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }
}
