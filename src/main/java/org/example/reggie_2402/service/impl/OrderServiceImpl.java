package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.common.BaseContext;
import org.example.reggie_2402.common.CustomException;
import org.example.reggie_2402.dto.OrdersDto;
import org.example.reggie_2402.entity.AddressBook;
import org.example.reggie_2402.entity.OrderDetail;
import org.example.reggie_2402.entity.Orders;
import org.example.reggie_2402.entity.ShoppingCart;
import org.example.reggie_2402.entity.User;
import org.example.reggie_2402.mapper.OrderMapper;
import org.example.reggie_2402.service.AddressBookService;
import org.example.reggie_2402.service.OrderDetailService;
import org.example.reggie_2402.service.OrderService;
import org.example.reggie_2402.service.ShoppingCartService;
import org.example.reggie_2402.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单业务实现
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单：购物车 → 订单 → 订单明细 → 清空购物车（六步，多表操作必须加事务）
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //第一步：根据当前用户 id 查询购物车数据：select * from shopping_cart where user_id = ?
        //购物车为空则抛业务异常，由全局异常处理器返回提示
        LambdaQueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartService.list(shoppingCartQueryWrapper);
        if (list == null || list.size() <= 0) {
            throw new CustomException("购物车中无数据，无法生成订单");
        }
        //第二步：完善订单属性：IdWorker.getIdStr() 雪花算法生成订单编号；默认状态 2（待派送，本课程模拟支付成功）
        orders.setNumber(IdWorker.getIdStr());
        orders.setStatus(2);
        orders.setUserId(BaseContext.getCurrentId());
        //第三步：遍历购物车累加 数量 × 单价 得到订单总价（金额计算必须用 BigDecimal，不能用 double，避免精度丢失）
        BigDecimal amount = new BigDecimal("0");
        for (ShoppingCart cart : list) {
            BigDecimal num = new BigDecimal(cart.getNumber());
            amount = amount.add(num.multiply(cart.getAmount()));
        }
        orders.setAmount(amount);
        //第四步：根据 addressBookId 查地址补充收件人/手机号/详细地址；根据用户 id 查用户补充用户名；设置下单时间与结账时间
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook != null) {
            orders.setPhone(addressBook.getPhone());
            orders.setAddress(addressBook.getDetail());
            orders.setConsignee(addressBook.getConsignee());
        }
        User user = userService.getById(BaseContext.getCurrentId());
        if (user != null) {
            orders.setUserName(user.getName());
        }
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        super.save(orders);
        //第五步：保存订单明细：把购物车项拷贝为 OrderDetail（忽略 id，因为订单明细有自己的雪花主键），
        //设置 orderId 后批量保存到订单明细表 order_detail
        Long orderId = orders.getId();
        List<OrderDetail> detailList = list.stream().map(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail, "id");
            orderDetail.setOrderId(orderId);
            return orderDetail;
        }).collect(Collectors.toList());
        orderDetailService.saveBatch(detailList);
        //第六步：清空当前用户购物车：delete from shopping_cart where user_id = ?
        LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(updateWrapper);
    }

    /**
     * 用户历史订单分页：按 BaseContext.getCurrentId() 过滤订单 → 转 OrdersDto → 回填订单明细 → 按结账时间降序
     */
    @Override
    public Page<OrdersDto> pageOrdersAndOrdersDetail(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        super.page(pageInfo, queryWrapper);
        //转 OrdersDto（records 除外），遍历每条订单查询订单明细集合并封装到 orderDetails 字段
        Page<OrdersDto> ordersDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<OrdersDto> ordersDtos = pageInfo.getRecords().stream().map(orders -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders, ordersDto);
            LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.eq(OrderDetail::getOrderId, orders.getId());
            ordersDto.setOrderDetails(orderDetailService.list(detailQueryWrapper));
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtos);
        return ordersDtoPage;
    }
}
