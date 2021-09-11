package com.atguigu.gmall.gmall_publisher.mapper;

import com.atguigu.gmall.gmall_publisher.bean.DAUPerHour;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@DS("hbase")
public interface DAUMapper {

    //提供三个方法

    /*
     * 查询当日日活
     * @author fw
     * @date 2021/9/8 11:37
     * @param null
     */

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
