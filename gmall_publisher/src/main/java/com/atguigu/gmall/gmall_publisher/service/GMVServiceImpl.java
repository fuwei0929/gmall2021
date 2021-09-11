package com.atguigu.gmall.gmall_publisher.service;

import com.atguigu.gmall.gmall_publisher.bean.GMVPerHour;
import com.atguigu.gmall.gmall_publisher.mapper.DAUMapper;
import com.atguigu.gmall.gmall_publisher.mapper.GMVMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @ClassName: GMVServiceImpl
 * @Author: fw
 * @Date: 2021/9/9 16:08
 * @Description: TODO
 */

@Service
public class GMVServiceImpl implements GMVService {

    @Autowired
    private GMVMapper mapper;


    @Override
    public Double getGMVByDate(String date) {

        System.out.println("开始计算每日的GMV");

        return mapper.getGMVByDate(date);
    }

    @Override
    public List<GMVPerHour> getGMVPerHourByDate(String date) {

        System.out.println("开始计算每日每小时的GMV");
        return mapper.getGMVPerHourBYDate(date);
    }


}
