package com.fw.realtime.app

import java.time.{Instant, LocalDateTime, LocalTime, ZoneId}
import java.time.format.DateTimeFormatter

import com.alibaba.fastjson.{JSON, JSONObject}
import com.atguigu.MyKafka.TopicConstants
import com.fw.realtime.beans.{StartLogInfo, StartUpLog}
import com.fw.realtime.utils.{MyKafkaUtil, RedisUtil}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

// 为了使用 RDD.saveToPhnenix
import org.apache.phoenix.spark._

/*

  采取精准一次 如果聚合类的计算：
          聚合的结果 , offset + 结果
          非聚合的计算 ,
 */
object DAUApp extends BaseApp {
  override var appname: String = "DAUApp"
  override var batchDuration: Int = 10

  def parseData(rdd: RDD[ConsumerRecord[String, String]]) = {

    rdd.map(record => {

      val jsonstr: String = record.value()
      //无法直接解析
      val jSONObject: JSONObject = JSON.parseObject(jsonstr)

      val log: StartUpLog = JSON.parseObject(jSONObject.getString("common"), classOf[StartUpLog])
      val info: StartLogInfo = JSON.parseObject(jSONObject.getString("start"), classOf[StartLogInfo])

      log.mergeStartInfo(info)

      log.ts = jSONObject.getString("ts").toLong

      val formatter1: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val formatter2: DateTimeFormatter = DateTimeFormatter.ofPattern("HH")
      //获取当前日期的值
      val dateTime: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(log.ts), ZoneId.of("Asia/Shanghai"))

      log.logDate = dateTime.format(formatter1)
      log.logHour = dateTime.format(formatter2)

      log
    })

  }

  /*
      同批次去重 ：只去当日中ts 最小的log

      同一天同一个设备 在当前批次产生的设备分组



   */
  def removeDupilcateLogInfoSameBatch(rdd: RDD[StartUpLog]) = {

    val rdd1: RDD[((String, String), Iterable[StartUpLog])] = rdd.map(startlog => {
      ((startlog.mid, startlog.logDate), startlog)
    }).groupByKey()

    val rdd2: RDD[StartUpLog] = rdd1.flatMap {
      case ((mid, logDate), logs) => {

        logs.toList.sortBy(_.ts).take(1)
      }
    }
    rdd2

  }

  def removeDupilcateLogInfoDiffBatch(rdd: RDD[StartUpLog]): RDD[StartUpLog] = {

    rdd.mapPartitions(partition => {

      //获取连接
      val jedis: Jedis = RedisUtil.getJedisClient()

      // 判断当前rdd中的 每个log的 mid是否已经在redis的集合中存在，如果存在就不要
      // 留下 判断条件为 true
      val filteredLogs: Iterator[StartUpLog] = partition.filter(log => !jedis.sismember(log.logDate, log.mid))

      //关闭连接
      jedis.close()


      //返回处理后的分区
      filteredLogs
    })
  }

  def main(args: Array[String]): Unit = {

    //构建StreamingContext
    context = new StreamingContext("local[*]", appname, Seconds(batchDuration))

    runApp {


      //调用MyKafkaUtil类的方法 获取一个KafkaStream
      val stream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(Array(TopicConstants.STARTUP_LOG),
        context, "connecttest")

      stream.foreachRDD(rdd => {

        if (!rdd.isEmpty()) {
          //获取offsets
          val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

          //将consumerRecord中的数据封装为样例类

          val rdd1: RDD[StartUpLog] = parseData(rdd)

          println("还没去重:" + rdd1.count())
          // 开始同批次去重  写个方法

          val rdd2: RDD[StartUpLog] = removeDupilcateLogInfoSameBatch(rdd1)

          println("同批次去重后:" + rdd2.count())

          //跨批次去重 利用redis 存在就不过滤 不存在就留下来

          val rdd3: RDD[StartUpLog] = removeDupilcateLogInfoDiffBatch(rdd2)
          //同一个rdd调了两次行动算子 可以做一个缓存 避免重复计算 默认storeage.level MEMEORY_ONLY
          rdd3.cache()

          println("跨批次去重后:" + rdd3.count())

          // 写入hbase    RDD -------->隐式转换 -----> ProductRDDFunctions.saveToPhoenix
          // 需要导入依赖 phonenix 以及他依赖的 spark-sql 的 依赖
          // 那么需要在建表 phoneix建立二级索引
          rdd3.saveToPhoenix("GMALL2021_STARTUPLOG",
            // 将RDD中的 T类型的每一个属性，写入表的哪些列
            Seq("AR", "BA", "CH", "IS_NEW", "MD", "MID", "OS", "UID", "VC", "ENTRY", "LOADING_TIME", "OPEN_AD_ID", "OPEN_AD_MS", "OPEN_AD_SKIP_MS", "LOGDATE", "LOGHOUR", "TS"),

            // new Configuration 只能读取 hadoop的配置文件，无法读取hbase-site.xml和hbase-default.xml
            HBaseConfiguration.create(),
            Some("hadoop102:2181")
          )


          //再将数据写入到 Redis当中 为下一次查询做准备
          rdd3.foreachPartition(partiton => {

            val jedis: Jedis = RedisUtil.getJedisClient()

            partiton.foreach(log => {

              jedis.sadd(log.logDate, log.mid)
              // 这个key要保存多久？  1天即可
              //
              jedis.expire(log.logDate, 60 * 60 * 24)

            })
            jedis.close()
          })
          //非聚合运算 offesets 维护到内置 _consumer_offsets + 幂等输出
          //提交offsets
          stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)

        }
      })

    }
  }
}

