package com.atguigu.gmall.gmall_publisher.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.gmall_publisher.bean.DAUPerHour;
import com.atguigu.gmall.gmall_publisher.bean.GMVPerHour;
import com.atguigu.gmall.gmall_publisher.service.DAUServiceImpl;

import com.atguigu.gmall.gmall_publisher.service.GMVServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: PublishController
 * @Author: fw
 * @Date: 2021/9/8 15:07
 * @Description: 请求处理 返回数据或界面
 */
@RestController
public class PublishController {

    /*
     *
     总数	[{"id":"dau","name":"当日日活数","value":1200},
            {"id":"new_mid","name":"新增设备数","value":233}]
            看别人数据要求的返回格式
            一个List集合 里面可以放JSONObject or JSONArray
            JSONObject 可以看成一个map
            * put 往里面写数据

    分时统计	{"yesterday":{"11":383,"12":123,"17":88,"19":200 },
            "today":{"12":38,"13":1233,"17":123,"19":688 }}

               一般使用JSONObject
     */

    //总数	http://localhost:8070/realtime-total?date=2021-08-15
    @Autowired
    private DAUServiceImpl dauService;
    @Autowired
    private GMVServiceImpl gmvService;
    @RequestMapping(value = "/realtime-total")
    public Object handle1(String date){
        ArrayList<JSONObject> resutl = new ArrayList<>();

        //获取数据
        Integer dauByDate = dauService.getDAUByDate(date);
        Integer newMidCountByDate = dauService.getNewMidCountByDate(date);
        Double gmvByDate = gmvService.getGMVByDate(date);

        JSONObject jsonObject1 = new JSONObject();
        JSONObject jsonObject2 = new JSONObject();
        JSONObject jsonObject3 = new JSONObject();
        //封装数据
        jsonObject1.put("id","name");
        jsonObject1.put("name","当日日活数");
        jsonObject1.put("value",dauByDate);
        //封装新增日活
        jsonObject2.put("id","new_id");
        jsonObject2.put("name","新增设备数");
        jsonObject2.put("value",newMidCountByDate);

        //封装每日GMV

        jsonObject3.put("id","order_amount");
        jsonObject3.put("name","当日交易额");
        jsonObject3.put("value",gmvByDate);
        //添加到返回的结果集

        resutl.add(jsonObject1);
        resutl.add(jsonObject2);
        resutl.add(jsonObject3);

        return resutl;
    }
    @RequestMapping(value = "/realtime-hours")
    public Object handle2(String id ,String date){

        JSONObject jsonObject = new JSONObject();

        String yestoday = LocalDate.parse(date).minusDays(1).toString();

        if ("dau".equals(id)){


            //今日的就是date

            //通过date算昨日的数据

            List<DAUPerHour> yestodayData = dauService.getDAUPerHourOfDate(yestoday);
            List<DAUPerHour> todayData = dauService.getDAUPerHourOfDate(date);

//        分时统计	{"yesterday":{"11":383,"12":123,"17":88,"19":200 },
//            "today":{"12":38,"13":1233,"17":123,"19":688 }}

            jsonObject.put("yesterday",parseData(yestodayData));
            jsonObject.put("today",parseData(todayData));
        }else  if (id.equals("order_amount")){

            List<GMVPerHour> todayGMVData = gmvService.getGMVPerHourByDate(date);
            List<GMVPerHour> yesTodayGMVData = gmvService.getGMVPerHourByDate(yestoday);


            jsonObject.put("today",handleGMVData(todayGMVData));
            jsonObject.put("yesterday",handleGMVData(yesTodayGMVData));


        }


        return jsonObject;
    }

    private JSONObject handleGMVData(List<GMVPerHour> GMVData) {
        JSONObject jsonObject = new JSONObject();

        for (GMVPerHour GMVDatum : GMVData) {

            jsonObject.put(GMVDatum.getPerhour(),GMVDatum.getGmv());
        }

        return jsonObject;

    }

    private JSONObject parseData(List<DAUPerHour> datas) {

        JSONObject jsonObject = new JSONObject();

        for (DAUPerHour data : datas) {


            jsonObject.put(data.getPerhour(),data.getDau());

        }
        return jsonObject;

    }


}
