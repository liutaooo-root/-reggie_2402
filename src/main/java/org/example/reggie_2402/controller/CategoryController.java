package org.example.reggie_2402.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.entity.Category;
import org.example.reggie_2402.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    @Autowired
    private CategoryService categoryService;


    @GetMapping("/page")
    public R<Page> page(int page,int pageSize) {
        //构建分页对象
        Page<Category> pageInfo=new Page<>(page,pageSize);
        //构造分页查询
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo,queryWrapper);
        //返回
        return R.success(pageInfo);
    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Category category) {
        //难点在于很多字段自动填充
        categoryService.save(category);
        return R.success("新增菜品成功");
    }

    /**
     * 根据分类id修改分类信息：请求方式 PUT，参数为 JSON（含 id）
     * updateTime/updateUser 由 MyMetaObjectHandler 自动填充，无需手动赋值
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    @DeleteMapping
    public R<String> delete(Long id) {
        log.info("删除分类：id={}", id);
        categoryService.remove(id);
        return R.success("分类信息删除成功");
    }
    /**
     * 查询分类列表：请求方式 GET，路径 /category/list，参数 type（1 菜品/2 套餐）
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
//添加根据分类类别查询的条件：type 不为空时才加该条件（方便后续查全部）
        queryWrapper.eq(category.getType() != null, Category::getType,
                category.getType());
//添加排序条件：sort 升序，sort 相同按更新时间降序（新加的排后面）
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
