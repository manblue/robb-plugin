package com.robb.zk;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceListener {

	private static Logger logger = LoggerFactory.getLogger(ServiceListener.class);
	private static Map<String, PathChildrenCache> listenerCache = new ConcurrentHashMap<String, PathChildrenCache>();
	public static boolean initPathChildrenCache(CuratorConnect connect) throws Exception {
		addNodeListener(connect, "/");
		
		return true;
	}
	
	/**为子节点添加watcher*/
	public static boolean addNodeListener(CuratorConnect connect,String pathNode) throws Exception {
        // 节点路径
		if (StringUtils.isBlank(pathNode)) {
	        System.out.println("当前ru节点："+(pathNode==null?"null":""));
			return false;
		}
        String nodePath = pathNode;
        String currentNode = "/"+connect.client.getNamespace()+nodePath;
        System.out.println("当前节点："+currentNode);

     // 为子节点添加watcher
        // PathChildrenCache: 监听数据节点的增删改，可以设置触发的事件
        final PathChildrenCache childrenCache = new PathChildrenCache(connect.client, nodePath, false);
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
//				boolean eNode = true;
				boolean nodeType = (event.getData().getStat().getEphemeralOwner() == 0);
				String nodeTypeStr = nodeType ? "永久" : "临时";
				System.out.println(nodeTypeStr+"节点["+event.getData().getPath()+"]事件触发："+event.getType().name());
				 // 通过判断event type的方式来实现不同事件的触发
                if (event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {  // 子节点初始化时触发
                    logger.info("节点[{}]下子节点初始化成功", currentNode);
                    System.out.println("节点["+currentNode+"]下子节点初始化成功");
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {  // 添加子节点时触发
                    //持久节点
                    if (nodeType) {
                        System.out.println("节点["+currentNode+"]下子节点["+event.getData().getPath()+"]初始化");
						addNodeListener(connect,event.getData().getPath());
					}
                    logger.info("节点[{}]下{}子节点[{}]添加成功,该子节点的数据为:{}", currentNode,nodeTypeStr,event.getData().getPath(),new String(event.getData().getData()));
                    System.out.println("节点["+currentNode+"]下"+nodeTypeStr+"子节点["+event.getData().getPath()+"]添加成功,该子节点的数据为:"+new String(event.getData().getData()));

                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {  // 删除子节点时触发
                    //持久节点
                    if (event.getData().getStat().getEphemeralOwner() == 0) {
//						addNodeListener(connect, nodePath+event.getData().getPath());
                    	PathChildrenCache tmp = listenerCache.remove(currentNode);
                    	if (tmp == null) {
                            System.out.println("节点["+currentNode+"]下"+nodeTypeStr+"子节点["+event.getData().getPath()+"]监听删除失败");
						}
                    	tmp.close();
                        System.out.println("节点["+currentNode+"]下"+nodeTypeStr+"子节点["+event.getData().getPath()+"]监听删除成功");

					}
                    logger.info("节点[{}]下{}子节点[{}]删除成功", currentNode,nodeTypeStr,event.getData().getPath());
                    System.out.println("节点["+currentNode+"]下"+nodeTypeStr+"子节点["+event.getData().getPath()+"]删除成功");

                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {  // 修改子节点数据时触发
                    //持久节点
                    if (event.getData().getStat().getEphemeralOwner() == 0) {
//						addNodeListener(connect, nodePath+event.getData().getPath());
					}
                    logger.info("节点[{}]下{}子节点[{}]数据更新成功,该子节点新的数据为:{}", currentNode,nodeTypeStr,event.getData().getPath(),new String(event.getData().getData()));
                    System.out.println("节点["+currentNode+"]下"+nodeTypeStr+"子节点["+event.getData().getPath()+"]数据更新成功,该子节点新的数据为:"+new String(event.getData().getData()));

                }
			}
		});
        
        listenerCache.put(currentNode, childrenCache);
        System.out.println("-------当前节点["+currentNode+"]添加listener成功");

		return true;
	}
}
