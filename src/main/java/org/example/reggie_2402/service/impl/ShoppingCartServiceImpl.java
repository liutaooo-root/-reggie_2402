package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.entity.ShoppingCart;
import org.example.reggie_2402.mapper.ShoppingCartMapper;
import org.example.reggie_2402.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * 购物车业务实现
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {
}
