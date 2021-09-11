package com.atguigu.gmall.gmall_publisher.service;

import com.atguigu.gmall.gmall_publisher.bean.GMVPerHour;

import java.util.List;

public interface GMVService {

    Double getGMVByDate(String date);



    List<GMVPerHour>getGMVPerHourByDate( String hour);
}
