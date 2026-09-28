package org.example.reggie_2402.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie_2402.entity.ShoppingCart;

/**
 * 购物车数据访问层
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
