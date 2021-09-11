package com.atguigu.gmall.gmall_publisher.mapper;


import com.atguigu.gmall.gmall_publisher.bean.GMVPerHour;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// 定义数据源头
@DS("mysql")
public interface GMVMapper {


    Double getGMVByDate(String date);

    /*
     * 查询当日新增设备数
     * @author fw
     * @date 2021/9/8 11:37
     * @param null
     */

    List<GMVPerHour> getGMVPerHourBYDate (String date);

}
