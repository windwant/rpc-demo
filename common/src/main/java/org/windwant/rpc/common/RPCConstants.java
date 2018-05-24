package org.windwant.rpc.common;

/**
 * Created by windwant on 2016/6/30.
 */
public interface RPCConstants {
    int ZK_SESSION_TIMEOUT = 5000;

    String ZK_REGISTRY_PATH = "/rpcregistry";

    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

}
