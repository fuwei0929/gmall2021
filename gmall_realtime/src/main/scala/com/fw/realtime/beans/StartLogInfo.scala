package com.fw.realtime.beans
/*

    主要是用来封装kafka 的数据 如何设计 StartUPLog 中的start 字段的值



 */
case class StartLogInfo(
                         entry:String,
                         loading_time:Int,
                         open_ad_id:Int,
                         open_ad_ms:Int,
                         open_ad_skip_ms:Int
                       )
