package com.robb.zk;

public class TestListener {

	public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        
        // 节点路径
        String nodePath = "/";
        ServiceListener.initPathChildrenCache(curatorConnect);
        Thread.sleep(1000000);

        // 关闭客户端
//        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

	}

}
