package org.example.reggie_2402.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.entity.User;
import org.example.reggie_2402.service.UserService;
import org.example.reggie_2402.utils.ValidateCodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 移动端用户控制器：验证码登录（无短信服务，验证码输出到控制台模拟发送）
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码：请求方式 POST，路径 /user/sendMsg，参数为 JSON（phone）
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的 4 位验证码，输出到控制台模拟发送短信，测试时从控制台查看验证码！
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            //将验证码保存到 Session，方便后续登录时比对：session.setAttribute(手机号, 验证码)
            session.setAttribute(phone, code);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录：请求方式 POST，路径 /user/login，参数为 JSON（phone、code）
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        //获取手机号与验证码（页面以 JSON 提交，用 Map 接收）
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        //从 Session 中获取保存的验证码，与页面提交的验证码比对，不一致则直接返回登录失败
        Object codeInSession = session.getAttribute(phone);
        if (codeInSession != null && codeInSession.equals(code)) {
            //比对成功后，根据手机号查询用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //判断当前手机号的用户是否存在，如果不存在则自动注册（手机号存入，状态默认 1 正常）
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //将登录用户 id 存入 Session
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
