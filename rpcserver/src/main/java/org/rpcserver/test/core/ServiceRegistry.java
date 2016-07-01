package org.rpcserver.test.core;

import org.apache.zookeeper.*;
import org.rpczookeeper.test.RPCConstants;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aayongche on 2016/6/30.
 */
public class ServiceRegistry {

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress){
        this.registryAddress = registryAddress;
    }

    public void registry(String data){
        if(data != null){
            ZooKeeper zk = connectSvr();
            if(zk != null){
                createNode(zk, data);
            }
        }
    }

    private ZooKeeper connectSvr(){
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, RPCConstants.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent event) {
                    if(event.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
            if(zk.exists(RPCConstants.ZK_REGISTRY_PATH, false) == null){
                zk.create(RPCConstants.ZK_REGISTRY_PATH, "ROOT".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return zk;
    }

    private void createNode(ZooKeeper zk, String data){
        try {
            String path = zk.create(RPCConstants.ZK_DATA_PATH, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
