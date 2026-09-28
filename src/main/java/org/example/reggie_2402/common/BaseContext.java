package org.example.reggie_2402.common;
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    /**
     * 设置值
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }
    /**
     * 获取值
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}