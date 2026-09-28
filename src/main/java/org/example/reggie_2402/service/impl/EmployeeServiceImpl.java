package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.entity.Employee;
import org.example.reggie_2402.mapper.EmployeeMapper;
import org.example.reggie_2402.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}
