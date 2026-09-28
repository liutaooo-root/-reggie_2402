package org.example.reggie_2402.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie_2402.entity.Employee;
//通过继承baseMapper中定义的通用的方法
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
