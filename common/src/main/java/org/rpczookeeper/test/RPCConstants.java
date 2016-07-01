package org.rpczookeeper.test;

/**
 * Created by aayongche on 2016/6/30.
 */
public interface RPCConstants {
    int ZK_SESSION_TIMEOUT = 5000;

    String ZK_REGISTRY_PATH = "/rpcregistry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
