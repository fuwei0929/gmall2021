package com.atguigu.gmall.gmall_publisher.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: GMVPerHour
 * @Author: fw
 * @Date: 2021/9/9 16:06
 * @Description: TODO
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMVPerHour {

    private String perhour;
    private Double gmv;

}
