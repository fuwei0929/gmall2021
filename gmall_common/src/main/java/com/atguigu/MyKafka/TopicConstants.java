package com.atguigu.MyKafka;

/*
 *
 * @author fw
 * @date 2021/9/6 19:37
 * @param null
 *
 * 接口就是为了放常量 因为 他不会被实例化 直接拿来用就好
 */


public interface TopicConstants {
    //用户欣慰数据的五个分类
    String STARTUP_LOG = "STARTUP_LOG";
    String ERROR_LOG = "ERROR_LOG";
    String DISPLAY_LOG = "DISPLAY_LOG";
    String PAGE_LOG = "PAGE_LOG";
    String ACTIONS_LOG = "ACTIONS_LOG";

    // 业务需求的模块
    String GMALL_ORDER_INFO = "GMALL_ORDER_INFO";
    String GMALL_ORDER_DETAIL = "GMALL_ORDER_DETAIL";
    String GMALL_USER_INFO = "GMALL_USER_INFO";

}
