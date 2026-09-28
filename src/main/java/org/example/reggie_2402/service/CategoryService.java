package org.example.reggie_2402.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie_2402.entity.Category;


public interface CategoryService extends IService<Category>  {
void remove(Long id);
}
