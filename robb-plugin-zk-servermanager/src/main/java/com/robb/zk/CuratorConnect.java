package com.robb.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.robb.zk.common.Constant;



/**
 * @program: zookeeper-connection
 * @description: 建立curator与zkserver的连接演示demo
 * @author: robb
 * @create: 2018-04-28 09:44
 **/
public class CuratorConnect {

	// Curator客户端
    public CuratorFramework client = null;
    // 集群模式则是多个ip
//    private static final String zkServerIps = "192.168.190.128:2181,192.168.190.129:2181,192.168.190.130:2181";
    private static final String zkServerIps = "192.168.0.126:2181";

    public CuratorConnect() {
    	/**
         * 同步创建zk示例，原生api是异步的
         * 这一步是设置重连策略
         *
         * ExponentialBackoffRetry构造器参数：
         *  curator链接zookeeper的策略:ExponentialBackoffRetry
         *  baseSleepTimeMs：初始sleep的时间
         *  maxRetries：最大重试次数
         *  maxSleepMs：最大重试时间
         */
    	RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
    	
        // 实例化Curator客户端，Curator的编程风格可以让我们使用方法链的形式完成客户端的实例化
        client = CuratorFrameworkFactory.builder() // 使用工厂类来建造客户端的实例对象
                .connectString(zkServerIps)  // 放入zookeeper服务器ip
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)  // 设定会话时间以及重连策略
               .namespace(Constant.rootPath) .build();  // 建立连接通道

        // 启动Curator客户端
        client.start();
	}
    
	 // 关闭zk客户端连接
	    public void closeZKClient() {
	        if (client != null) {
	            this.client.close();
	        }
	    }
	

}
