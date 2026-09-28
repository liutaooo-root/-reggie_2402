package org.example.reggie_2402.dto;

import org.example.reggie_2402.entity.Dish;
import org.example.reggie_2402.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
    //菜品对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();
    private String categoryName;
    private Integer copies;
}
