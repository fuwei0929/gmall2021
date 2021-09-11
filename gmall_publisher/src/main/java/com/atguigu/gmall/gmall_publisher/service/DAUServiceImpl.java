package com.atguigu.gmall.gmall_publisher.service;

import com.atguigu.gmall.gmall_publisher.bean.DAUPerHour;
import com.atguigu.gmall.gmall_publisher.mapper.DAUMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName: DAUServiceImpl
 * @Author: fw
 * @Date: 2021/9/8 11:38
 * @Description: TODO
 */
@Service
public class DAUServiceImpl implements DAUService {


    @Autowired
    private DAUMapper mapper;
    @Override
    public Integer getDAUByDate(String date) {
        System.out.println("日活统计中");
        return mapper.getDAUByDate(date);
    }

    @Override
    public Integer getNewMidCountByDate(String date) {

        System.out.println("计算新增设备数");
        return mapper.getNewMidCountByDate(date);
    }

    @Override
    public List<DAUPerHour> getDAUPerHourOfDate(String date) {

        System.out.println("计算每日每小时的新增设备数量");
        return mapper.getDAUPerHourOfDate(date);
    }
}
