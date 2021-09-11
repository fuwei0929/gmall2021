package com.atguigu.gmall.gmall_publisher.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: DAUEachDay
 * @Author: fw
 * @Date: 2021/9/8 11:28
 * @Description: TODO
 *
 * 封装查询出来的数据的 最小的粒度 封装进去
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DAUPerHour {

    private Integer dau;
    private String perhour;
}
