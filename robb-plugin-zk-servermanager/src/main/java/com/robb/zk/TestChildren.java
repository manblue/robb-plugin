package com.robb.zk;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

public class TestChildren {

	public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        
        // 节点路径
        String nodePath = "/";
        
        
     // 为子节点添加watcher
        // PathChildrenCache: 监听数据节点的增删改，可以设置触发的事件
        final PathChildrenCache childrenCache = new PathChildrenCache(curatorConnect.client, nodePath, true);

        /**
         * StartMode: 初始化方式
         * POST_INITIALIZED_EVENT：异步初始化，初始化之后会触发事件
         * NORMAL：异步初始化
         * BUILD_INITIAL_CACHE：同步初始化
         */
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        // 列出子节点数据列表，需要使用BUILD_INITIAL_CACHE同步初始化模式才能获得，异步是获取不到的
        List<ChildData> childDataList = childrenCache.getCurrentData();
        System.out.println("当前节点的子节点详细数据列表：");
        for (ChildData childData : childDataList) {
            System.out.println(childData.getStat().toString()+"\t* 子节点路径：" + new String(childData.getPath()) + "，该节点的数据为：" + new String(childData.getData()));
        }

        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
					throws Exception {
				 // 通过判断event type的方式来实现不同事件的触发
                if (event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {  // 子节点初始化时触发
                    System.out.println("\n--------------\n");
                    System.out.println("子节点初始化成功");
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {  // 添加子节点时触发
                    if (event.getData().getPath().trim().contains(nodePath)) {
                        System.out.println("\n--------------\n");
                        System.out.print(event.getData().getStat().toString()+"子节点：" + event.getData().getPath() + " 添加成功，");
                        System.out.println("该子节点的数据为：" + new String(event.getData().getData()));
                    }
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {  // 删除子节点时触发
                    if (event.getData().getPath().trim().contains(nodePath)) {
                        System.out.println("\n--------------\n");
                        System.out.println(event.getData().getStat().toString()+"子节点：" + event.getData().getPath() + " 删除成功");
                    }
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {  // 修改子节点数据时触发
                    if (event.getData().getPath().trim().contains(nodePath)) {
                        System.out.println("\n--------------\n");
                        System.out.print(event.getData().getStat().toString()+"子节点：" + event.getData().getPath() + " 数据更新成功，");
                        System.out.println("子节点：" + event.getData().getPath() + " 新的数据为：" + new String(event.getData().getData()));
                    }
                }
            }
		});
        
        
        
        
        
        
        Thread.sleep(1000000);

        // 关闭客户端
//        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

	}

}
