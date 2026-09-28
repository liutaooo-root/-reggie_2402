package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@Slf4j
@SpringBootApplication
@ServletComponentScan//扫描@WebFilter注解过滤器
@EnableTransactionManagement//开启事物注解扫描
public class Reggie_2402Application {
    public static void main(String[] args) {
        SpringApplication.run(Reggie_2402Application.class, args);
        log.info("启动成功");//log可以保存到日志，方便查询错误
    }
}
