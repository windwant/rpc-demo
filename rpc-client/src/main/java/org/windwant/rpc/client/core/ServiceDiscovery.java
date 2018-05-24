package org.windwant.rpc.client.core;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.windwant.rpc.common.RPCConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务发现即监测
 * Created by windwant on 2016/6/30.
 */
public class ServiceDiscovery {

    //同步工具 zookeeper连接
    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<String>();

    private String registryAddress;

    ServiceDiscovery(String registryAddress){
        this.registryAddress = registryAddress;
        //连接zookeeper服务
        ZooKeeper zk = connectServer();
        if(zk != null){
            //监听节点
            watchNode(zk);
        }
    }

    /**
     * 随机获取服务器地址
     * @return
     */
    public String discory(){
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
            }
        }
        return data;
    }

    /**
     * 连接zookeeper服务
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, RPCConstants.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        //连接完成，唤醒阻塞
                        latch.countDown();
                    }
                }
            });
            //阻塞等待连接完成
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk){
        try {
            //获取服务节点 并监听
            List<String> nodeList = zk.getChildren(RPCConstants.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent event) {
                    if(event.getType() == Event.EventType.NodeChildrenChanged){
                        watchNode(zk);
                    }
                }
            });

            List<String> dataList = new ArrayList<String>();
            for(String node: nodeList){
                //获取节点数据（服务器地址 host:port）
                byte[] data = zk.getData(RPCConstants.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(data));
            }
            this.dataList = dataList;
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
