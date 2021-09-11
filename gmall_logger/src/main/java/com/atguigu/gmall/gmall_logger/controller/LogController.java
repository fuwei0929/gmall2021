package com.atguigu.gmall.gmall_logger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.MyKafka.TopicConstants;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: LogController
 * @Author: fw
 * @Date: 2021/9/3 15:19
 * @Description: TODO
 *
 * 收客户端发送的日志，收成功后，不需要返回客户端界面
 *
 *      客户端发送的url：
 *
 *      配置端口：8888
 *
 *      项目名：gmall_log
 *
 *      以及携带的参数名字：
 *          默认为param
 */
@RestController
@Log4j // 相当于自动创建了 Logger logger = Logger.getLogger(LogController.class);
public class LogController {

    @Autowired
    private KafkaTemplate producer;

    @RequestMapping(value = "/hello")
    public Object handletest() {

        System.out.println("hello");

        return "hello";
    }

    @RequestMapping(value = "/applog")
    public Object handlelog(String param){

        JSONObject jsonObject = JSON.parseObject(param);

        if (jsonObject.getString("start")!=null){

            producer.send(TopicConstants.STARTUP_LOG,param);
        }
        if (jsonObject.getString("actions")!=null){

            producer.send(TopicConstants.ACTIONS_LOG,param);
        }
        if (jsonObject.getString("displays")!=null){

            producer.send(TopicConstants.DISPLAY_LOG,param);
        }
        if (jsonObject.getString("err")!=null){

            producer.send(TopicConstants.ERROR_LOG,param);
        }

        if (jsonObject.getString("page") != null){

            producer.send(TopicConstants.PAGE_LOG,param);
        }

        log.info(param);

        return "success";
    }
}