package org.rpcclient.test.core;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.rpczookeeper.test.RPCConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by aayongche on 2016/6/30.
 */
public class ServiceDiscovery {

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<String>();

    private String registryAddress;

    ServiceDiscovery(String registryAddress){
        this.registryAddress = registryAddress;
        ZooKeeper zk = connectServer();
        if(zk != null){
            watchNode(zk);
        }
    }

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

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, RPCConstants.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
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
            List<String> nodeList = zk.getChildren(RPCConstants.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent event) {
                    if(event.getType() == Event.EventType.NodeChildrenChanged){
                        watchNode(zk);
                    }
                }
            });

            List<String> dataList = new ArrayList<String>();
            for(String node: nodeList){
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
