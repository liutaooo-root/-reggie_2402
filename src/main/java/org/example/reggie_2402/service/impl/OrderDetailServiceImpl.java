package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.entity.OrderDetail;
import org.example.reggie_2402.mapper.OrderDetailMapper;
import org.example.reggie_2402.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * 订单明细业务实现
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail>
        implements OrderDetailService {
}
