package com.fw.realtime.app

import java.sql.{Connection, PreparedStatement}
import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

import com.alibaba.fastjson.JSON
import com.atguigu.MyKafka.TopicConstants
import com.fw.realtime.beans.OrderInfo
import com.fw.realtime.utils.{JDBCUtil, MyKafkaUtil}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object GMVApp extends BaseApp {

  override var appname: String = "GMVApp"
  override var batchDuration: Int = 10
  val groupid: String = "connecttest"

  def changeDatatoRecord(rdd: RDD[ConsumerRecord[String, String]]):RDD[OrderInfo] = {

    //map转换封住 数据
    val reult: RDD[OrderInfo] = rdd.map(record => {

      val info: OrderInfo = JSON.parseObject(record.value(), classOf[OrderInfo])

      //封装额外的属性 需要两个formatter

      val formatter1: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val formatter2: DateTimeFormatter = DateTimeFormatter.ofPattern("HH")
      val formatter3: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      //获取当前日期的值
      val dateTime: LocalDateTime = LocalDateTime.parse(info.create_time, formatter3)

      info.create_date = dateTime.format(formatter1);
      info.create_hour = dateTime.format(formatter2);

      info
    })
    reult
  }


  def writeResultAndOffsetsToMysql(result: Array[((String, String), Double)], offsetRanges: Array[OffsetRange]) = {

    //获取连接

    var connection:Connection = null
    var ps1: PreparedStatement=null
    var ps2: PreparedStatement=null


    //存在就更新 保证幂等输出
    val sql1:String = "INSERT INTO  gmvstats VALUES(?,?,?) ON DUPLICATE KEY UPDATE gmv = gmv + VALUES(gmv);"

    val sql2:String = "INSERT INTO  offsets VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE offset =  VALUES(offset);"

    try {
      connection = JDBCUtil.getConnection()


      //关闭自动提交 才能批处理
      connection.setAutoCommit(false)

      ps1 = connection.prepareStatement(sql1)
      ps2 = connection.prepareStatement(sql2)

      for (((creat_date,creat_hour),gmv) <- result) {
        ps1.setString(1 ,creat_date)
        ps1.setString(2 ,creat_hour)
        ps1.setBigDecimal(3 ,new java.math.BigDecimal(gmv))

        ps1.addBatch()
      }
      //成功了多少条
      val ints: Array[Int] = ps1.executeBatch()
      println("计算结果写入成功的个数： " + ints)

      for (offsetRange <- offsetRanges) {
        ps2.setString(1,groupid)
        ps2.setString(2,offsetRange.topic)
        ps2.setInt(3,offsetRange.partition)
        ps2.setLong(4,offsetRange.untilOffset)

        ps2.addBatch()
      }
      //提交成功的数量
      val ints1: Array[Int] = ps2.executeBatch()

      println("偏移量写入成功的个数" + ints1)
      //最后提交事务

      connection.commit()

    } catch {
      case e :Exception =>
        println("程序有点问题")

        e.printStackTrace()
        connection.rollback()
    } finally {

      if (ps1 != null){

        ps1.close()
      }
      if (ps2 != null){

        ps1.close()
      }
      if (connection != null){
        connection.close()
      }

    }




  }

  def main(args: Array[String]): Unit = {

    //给StreamingContext 赋值
     context =  new StreamingContext("local[*]",appname,Seconds(batchDuration))

    val partitionToLong: Map[TopicPartition, Long] = JDBCUtil.readHitoryOffsetsFromMysql(groupid)


    runApp{

      //将offset及计算结果 存储到 mysql 聚合类的计算 创建DS 那么需要连接Mysql 整个JDBCUtil 连接池工具类
      //基于已经存在的 offset来创建 kafkaDS
      // 获取offsetrange


      val ds: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(Array(TopicConstants.GMALL_ORDER_INFO), context, "connecttest",
        true, partitionToLong)

          //测试连接

      println("ppppp")

          ds.foreachRDD(rdd =>{

            if (!rdd.isEmpty()){
              //获取偏移量
              //获取offsets
              val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
              println(offsetRanges)
              // 从kafka 消费数据 样例类来保存数据
              val rdd1: RDD[OrderInfo] = changeDatatoRecord(rdd)

              val rdd2: RDD[((String, String), Iterable[Double])] = rdd1.map(orderinfo => {
                ((orderinfo.create_date, orderinfo.create_hour), orderinfo.total_amount)
              }).groupByKey()

              //计算结果收集到Driver端
              val rdd3: RDD[((String, String), Double)] = rdd2.mapValues(total_amount=>total_amount.sum)

              val result: Array[((String, String), Double)] = rdd3.collect
              println("收集完成")
              for (elem <- result) {

                println(elem)
              }



              //将计算结果和 offset 一同写入到Mysql Mysql建表 建表为联合主键

             writeResultAndOffsetsToMysql(result,offsetRanges)


            }
          })





    }
  }
}
