package org.example.reggie_2402.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.entity.Employee;
import org.example.reggie_2402.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //密码加密
        String password = employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        //调用Service层方法从数据库中获取员工信息
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp=employeeService.getOne(queryWrapper);
        //比较
        if(emp==null){
            return R.error("登录失败");
        }
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
        if (emp.getStatus()==0){
            return R.error("账号已被封禁");
        }
        //登录成功，返回员工信息
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出：清除 Session 中保存的当前登录员工的 id，页面会跳回登录页（页面 JS 已处理）
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
}
