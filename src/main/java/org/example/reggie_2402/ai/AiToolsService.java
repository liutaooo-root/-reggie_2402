package org.example.reggie_2402.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.reggie_2402.common.BaseContext;
import org.example.reggie_2402.entity.Dish;
import org.example.reggie_2402.entity.OrderDetail;
import org.example.reggie_2402.entity.Orders;
import org.example.reggie_2402.service.DishService;
import org.example.reggie_2402.service.OrderDetailService;
import org.example.reggie_2402.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent 工具层：把项目已有的查询能力封装为"工具"，供 Agent 按意图调用；
 * 工具返回文本而非对象，目的是理解"工具输出作为模型上下文"的最简形式。
 */
@Service
public class AiToolsService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private DishService dishService;

    /** 工具 1：查询当前登录用户最近一笔订单（订单号、下单时间、金额、状态） */
    public String queryLatestOrder() {
        //按 BaseContext.getCurrentId() 查当前用户订单，按下单时间降序取第一条
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId())
                .orderByDesc(Orders::getOrderTime).last("limit 1");
        Orders order = orderService.getOne(queryWrapper);
        //无订单时返回提示文本（兜底），有则拼装为简洁文本
        if (order == null) {
            return "当前用户没有订单记录";
        }
        return String.format("订单号：%s，下单时间：%s，金额：%s，状态：%s",
                order.getNumber(), order.getOrderTime(), order.getAmount(), statusText(order.getStatus()));
    }

    /** 工具 2：按订单 id 查询订单详情（菜品名称、数量、单价），需对 orderId 为 null 做空值保护 */
    public String queryOrderDetail(Long orderId) {
        if (orderId == null) {
            return "未找到订单信息";
        }
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> details = orderDetailService.list(queryWrapper);
        if (details.isEmpty()) {
            return "未找到该订单的明细信息";
        }
        StringBuilder sb = new StringBuilder();
        for (OrderDetail d : details) {
            sb.append(d.getName()).append(" x").append(d.getNumber())
                    .append("（单价 ").append(d.getAmount()).append("）；");
        }
        return sb.toString();
    }

    /** 工具 3：按名称模糊查询启售菜品（名称、价格）；关键词为 null 时查全部启售菜品 */
    public String queryDish(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "";
        }
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Dish::getName, name).eq(Dish::getStatus, 1);
        List<Dish> list = dishService.list(queryWrapper);
        if (list.isEmpty()) {
            return "未找到相关启售菜品";
        }
        StringBuilder sb = new StringBuilder();
        for (Dish dish : list) {
            sb.append(dish.getName()).append("：").append(dish.getPrice()).append(" 元；");
        }
        return sb.toString();
    }

    /** 订单状态码转文本：1 待付款 2 待派送 3 已派送 4 已完成 5 已取消 */
    private String statusText(Integer status) {
        String[] texts = {"", "待付款", "待派送", "已派送", "已完成", "已取消"};
        return status != null && status >= 1 && status <= 5 ? texts[status] : "未知";
    }
}
