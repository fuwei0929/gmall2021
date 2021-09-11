package com.atgugui.canal;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.atguigu.MyKafka.TopicConstants;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @ClassName: MyCanal
 * @Author: fw
 * @Date: 2021/9/4 13:16
 * @Description: TODO
 * ①创建一个 Canal客户端对象
 * CanalConnector 接口 看他的实现类
 * <p>
 * SimpleCanalConnector： 普通模式
 * <p>
 * ClusterCanalConnector: 集群版本connector实现，自带了failover功能.
 * 可以借助zk，在两台机器安装canal，一台默认为active，另一台我standby，类似hadoop HA！
 * <p>
 * 使用 CanalConnectors 工具类 创建客户端对象！
 * <p>
 * ②使用客户端对象 连接上 canal server
 * ③订阅canal server收到的binlog日志
 * ④拉取canal server端的 日志
 * ⑤解析日志
 * Message: 代表拉取到的一批SQL引起的数据变化情况。
 * <p>
 * List<Entry> entries：  所有SQL引起的数据变化
 * <p>
 * Entry： 一条SQL引起的数据变化
 * <p>
 * private CanalEntry.Header header_
 * private Object tableName_ :  当前SQL是操作的哪张表
 * <p>
 * private CanalEntry.EntryType entryType_： 当前SQL的类型
 * begin : 事务开启类型
 * <p>
 * commit | rollback : 事务结束类型
 * <p>
 * insert|update|delete : RowData类型(引起每行数据变化的类型)
 * <p>
 * <p>
 * private ByteString storeValue_ ： 存储引起的数据的变化。序列化，无法直接使用！
 * 使用 RowChange转换为 RowChange对象
 * <p>
 * <p>
 * RowChange： 转换后的storeValue
 * <p>
 * EVentType： 当前SQL是什么具体SQL
 * <p>
 * RowDataList : 当前sql引起的所有行的变化
 * <p>
 * RowData：   一行数据的变化
 * Column: 一列数据的变化
 * <p>
 * 假装是Mysql的一个从机 去偷数据
 */
public class MyCanal {

    public static void main(String[] args) throws InvalidProtocolBufferException, InterruptedException {


        //        SocketAddress address,: canal server的主机名和端口号。参考  canal.properties
        //        canal.ip
        //        canal.port
        //        String destination: 选择当前客户端订阅的数据来自哪个mysql,填写对应的instance.properties所在的目录，
        //        参考canal.properties
        //        canal.destinations = example,example2,example3 不同的目录 拥有不同的 instance.properties
        //        String username: 1.1.4之后提供
        //        String password: 1.1.4之后提供

        //如何创建一个canal client 参数 主机名端口号（参考canal.properties配置文件） 选择客户端订阅的数据来自哪个MySql
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop103", 11111),
                "example", null, null);
        int batchsize = 100;
        //获取连接
        canalConnector.connect();
        //订阅 可以传参 也可以不传参  订阅哪个库 那个表的操作变化
        canalConnector.subscribe("gmall2021.order_info");

        while (true) {
            //get方法 拉去数据 一次最多拿多少 有多少拿多少 没有就是空
            Message message = canalConnector.get(batchsize);


            if (message.getId() == -1) {

                System.out.println("没有数据等会再次尝试。。。。。");
                Thread.sleep(5000);
                continue;
            }
            System.out.println(message);

//            获取所有的数据变化
            List<CanalEntry.Entry> entries = message.getEntries();
//             遍历entrys 获得每一个sql的变化
            for (CanalEntry.Entry entry : entries) {

                String tableName = entry.getHeader().getTableName();

                //说明这个语句是 CRUD 语句
                if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {

                    ByteString storeValue = entry.getStoreValue();
                    //解析 entry
                    parseData(storeValue);
                }

            }


        }


    }

    private static void parseData(ByteString storeValue) throws InvalidProtocolBufferException {

        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(storeValue);

        if (rowChange.getEventType().equals(CanalEntry.EventType.INSERT)) {

            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();

            JSONObject jsonObject = new JSONObject();

            for (CanalEntry.RowData rowData : rowDatasList) {

                List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();

                for (CanalEntry.Column column : afterColumnsList) {

                    jsonObject.put(column.getName(), column.getValue());

                }
            }
            System.out.println("-----------------------------");
//            System.out.println(jsonObject.toJSONString());

            Myproducer.sendData(TopicConstants.GMALL_ORDER_INFO,jsonObject.toJSONString());
        }


    }


}
