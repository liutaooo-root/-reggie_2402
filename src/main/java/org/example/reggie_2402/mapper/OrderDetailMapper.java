package org.example.reggie_2402.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie_2402.entity.OrderDetail;

/**
 * 订单明细数据访问层
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
