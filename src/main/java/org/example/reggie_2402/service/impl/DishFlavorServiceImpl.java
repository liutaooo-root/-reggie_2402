package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.dto.DishDto;
import org.example.reggie_2402.entity.Dish;
import org.example.reggie_2402.entity.DishFlavor;
import org.example.reggie_2402.mapper.DishFlavorMapper;
import org.example.reggie_2402.service.DishFlavorService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {


}
