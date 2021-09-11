package com.fw.realtime.testjson


import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

import com.alibaba.fastjson.{JSON, JSONObject}
import com.fw.realtime.beans.{StartLogInfo, StartUpLog}
import org.apache.spark.sql.catalyst.expressions.DateFormatClass

object TestJSONParse {



  def main(args: Array[String]): Unit = {

      val str = "{\"common\":{\"ar\":\"110000\",\"ba\":\"iPhone\",\"ch\":\"Appstore\",\"is_new\":\"1\",\"md\":\"iPhone Xs Max\",\"mid\":\"mid_260\",\"os\":\"iOS 13.3.1\",\"uid\":\"258\",\"vc\":\"v2.1.134\"},\"start\":{\"entry\":\"notice\",\"loading_time\":6267,\"open_ad_id\":11,\"open_ad_ms\":2882,\"open_ad_skip_ms\":0},\"ts\":1630839793000}"

          val jSONObject: JSONObject = JSON.parseObject(str)

    println(jSONObject.getString("common"))

    val log: StartUpLog = JSON.parseObject(jSONObject.getString("common"),classOf[StartUpLog])
     val info: StartLogInfo = JSON.parseObject(jSONObject.getString("start"),classOf[StartLogInfo])
    log.mergeStartInfo(info)

    println(log.toString)

    log.ts = jSONObject.getString("ts").toLong

    val formatter1: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatter2: DateTimeFormatter = DateTimeFormatter.ofPattern("HH")
    //获取当前日期的值
    val dateTime: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(log.ts),ZoneId.of("Asia/Shanghai"))

    log.logDate = dateTime.format(formatter1)
    log.logHour = dateTime.format(formatter2)

    log
  }

}
