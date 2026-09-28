package org.example.reggie_2402.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.dto.OrdersDto;
import org.example.reggie_2402.entity.OrderDetail;
import org.example.reggie_2402.entity.Orders;
import org.example.reggie_2402.service.OrderDetailService;
import org.example.reggie_2402.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单控制器：管理端订单明细 + 用户端下单与历史订单查询
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 订单信息分页查询（管理端订单明细）：请求方式 GET，路径 /order/page，
     * 参数 page、pageSize、number（可空，订单号模糊）、beginTime/endTime（可空，下单时间范围）
     */
    @GetMapping("/page")
    public R<Page<OrdersDto>> page(int page, int pageSize, String number,
                                   String beginTime, String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //按订单号模糊查询、按下单时间范围查询（beginTime 与 endTime 都不为空时才加 between）、按下单时间降序排序
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.between(beginTime != null && endTime != null,
                Orders::getOrderTime, beginTime, endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo, queryWrapper);
        //分页结果转 OrdersDto（BeanUtils 拷贝，records 除外），遍历每条订单查询订单详情集合并封装到 orderDetails 字段
        Page<OrdersDto> ordersDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<OrdersDto> ordersDtos = pageInfo.getRecords().stream().map(orders -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders, ordersDto);
            //查询当前订单对应的订单明细：select * from order_detail where order_id = ?
            LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.eq(OrderDetail::getOrderId, orders.getId());
            ordersDto.setOrderDetails(orderDetailService.list(detailQueryWrapper));
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtos);
        return R.success(ordersDtoPage);
    }

    /**
     * 用户下单：请求方式 POST，路径 /order/submit，参数为 JSON（addressBookId、remark 等）
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用户历史订单分页查询（来单/我的订单页）：请求方式 GET，路径 /order/userPage，
     * 按当前登录用户 id 查订单及订单详情，按结账时间降序（最近的订单排前面）
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> ordersPage(Integer page, Integer pageSize) {
        Page<OrdersDto> pageInfo = orderService.pageOrdersAndOrdersDetail(page, pageSize);
        return R.success(pageInfo);
    }

    /**
     * 修改订单状态（待派送 → 已派送、已派送 → 已完成）：请求方式 PUT，参数为 JSON（含 id 与 status）
     * 订单状态码：1 待付款 2 待派送 3 已派送 4 已完成 5 已取消
     */
    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        log.info("修改订单状态：{}", orders);
        orderService.updateById(orders);
        return R.success("订单状态更新成功");
    }
}
