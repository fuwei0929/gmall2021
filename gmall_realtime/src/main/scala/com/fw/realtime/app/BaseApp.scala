package com.fw.realtime.app

import org.apache.spark.streaming.StreamingContext

import scala.util.control.BreakControl


/*

  实时计算又很多的计算逻辑 每个计算逻辑前后的基本上都是模板操作

  写一个基类 就可以 省去很多的代码

  编写一个sparkstreaming app的套路

  1、创建一个StreamingContext

           2、 从StreamingContext中获取DS

           3、  每个需求需要根据业务进行不同的转换

  4、启动app

  5、阻塞线程

  少去模板的操作 ，使用spark的 控制抽象 来实现

 */
abstract class BaseApp {

  //抽象属性 子类 继承必须重新赋值
  var appname:String
  var batchDuration:Int
  var context:StreamingContext = null
  def runApp(op: => Unit) {
    try {
      //子类传入的计算逻辑
      op

      context.start()

      context.awaitTermination()

    } catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }

}
