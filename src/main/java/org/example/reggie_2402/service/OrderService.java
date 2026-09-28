package org.example.reggie_2402.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie_2402.dto.OrdersDto;
import org.example.reggie_2402.entity.Orders;

/**
 * 订单业务接口
 */
public interface OrderService extends IService<Orders> {
    /** 用户下单：购物车 → 订单 → 订单明细 → 清空购物车 */
    void submit(Orders orders);

    /** 用户历史订单分页：按当前登录用户查询订单及订单明细，按结账时间降序 */
    Page<OrdersDto> pageOrdersAndOrdersDetail(int page, int pageSize);
}
