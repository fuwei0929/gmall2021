package com.atguigu.gmall.gmall_publisher.service;

import com.atguigu.gmall.gmall_publisher.bean.DAUPerHour;

import java.util.List;

public interface DAUService {


    Integer getDAUByDate(String date);

    /*
     * 查询当日新增设备数
     * @author fw
     * @date 2021/9/8 11:37
     * @param null
     */

    Integer getNewMidCountByDate(String date);

    /*
     * 查询某一天，各时段的DAU
     * @author fw
     * @date 2021/9/8 11:37
     * @param null
     */

    List<DAUPerHour> getDAUPerHourOfDate(String date);
}
