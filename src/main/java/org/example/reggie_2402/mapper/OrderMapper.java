package org.example.reggie_2402.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie_2402.entity.Orders;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
