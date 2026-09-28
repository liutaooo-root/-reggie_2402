package org.example.reggie_2402.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie_2402.common.BaseContext;
import org.example.reggie_2402.common.R;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter  implements Filter {
    //路径匹配器，支持通配符 **
    public  static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //获取请求的url
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        String requestURI=request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);
        //编写白名单
        //定义不需要处理的请求路径（登录/退出、静态页面、用户端登录相关）
        String[] urls = new String[]{
                "/employee/login", "/employee/logout", "/backend/**",
                "/front/**", "/common/**", "/user/sendMsg", "/user/login"
        };
        //2.判断本次请求是否需要处理（白名单匹配）
        boolean check=check(urls,requestURI);
        //3.如果不需要处理，则直接放行（放行后请求交给 DispatcherServlet 处理）
        if (check){
            filterChain.doFilter(request, response);
            return;
        }
        //4.判断登录状态，如果已登录（Session 中有 employee 属性），则直接放行；
        if (request.getSession().getAttribute("employee") != null) {
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request, response);
            return;
        }
        //5.判断登录状态，如果移动端用户已登录，则直接放行，并把用户 id 存入 ThreadLocal 供后续业务使用（如地址、购物车、订单按用户隔离）
        if (request.getSession().getAttribute("user") != null) {
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            return;
        }
//6.如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据（页面 JS 检测到NOTLOGIN 会自动跳回登录页）
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }
    /** 路径匹配：遍历白名单，任一模式能匹配本次请求则放行 */
    public boolean check(String[] urls, String requestURI) {
       for (String url : urls) {
           if (PATH_MATCHER.match(url, requestURI)) {
               return true;
           }
       }
        return false;
    }
}
