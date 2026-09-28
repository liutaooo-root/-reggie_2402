package org.example.reggie_2402.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.reggie_2402.common.BaseContext;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.entity.ShoppingCart;
import org.example.reggie_2402.service.ShoppingCartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车控制器：购物车数据按用户隔离，全部通过 BaseContext 获取当前用户 id
 */
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    private static final Logger log = LoggerFactory.getLogger(ShoppingCartController.class);

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车：请求方式 POST，路径 /shoppingCart/add，参数为 JSON（可能含 dishId 或 setmealId）
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}", shoppingCart);
        //设置用户 id，指定当前是哪个用户的购物车数据（ThreadLocal 中获取）
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //根据菜品 id 或套餐 id 查询购物车中是否已存在该商品：
        //select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        if (dishId != null) {
            //菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne != null) {
            //已存在：在原来数量基础上加 1 后更新：update shopping_cart set number = number+1 where id = ?
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //不存在：数量设为 1，设置创建时间后插入购物车表
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 减少购物车商品数量：数量 >1 递减，否则删除该记录（判空保护：查不到时返回错误）
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        //同 add() 构造查询条件查出购物车记录，数量 >1 递减 1 后更新，否则从购物车删除该记录
        Long currentId = BaseContext.getCurrentId();
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne == null) {
            return R.error("购物车中无该商品");
        }
        Integer number = cartServiceOne.getNumber();
        if (number > 1) {
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            shoppingCartService.removeById(cartServiceOne);
        }
        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车：请求方式 GET，路径 /shoppingCart/list；按当前用户 id 查询，按添加时间升序返回页面（先加的先展示）
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        return R.success(shoppingCartService.list(queryWrapper));
    }

    /**
     * 清空购物车：请求方式 DELETE，路径 /shoppingCart/clean；delete from shopping_cart where user_id = ?
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
