package org.example.reggie_2402.dto;

import org.example.reggie_2402.entity.Setmeal;
import org.example.reggie_2402.entity.SetmealDish;
import lombok.Data;
import java.util.List;
@Data
public class SetmealDto extends Setmeal {
    private List<SetmealDish> setmealDishes;
    private String categoryName;
}
