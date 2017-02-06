package org.rpcclient.test.core;

import org.rpcclient.test.RpcClient;
import org.rpcserver.test.pojo.RpcRequest;
import org.rpcserver.test.pojo.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Created by windwant on 2016/6/30.
 */
public class RpcProxy implements InvocationHandler {

    private String serverAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(Class<?> interfaceCls){
        T rst = null;
        try {
            rst = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{interfaceCls}, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rst;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterType(method.getParameterTypes());
        request.setParameters(args);
        if(serviceDiscovery != null){
            serverAddress = serviceDiscovery.discory();
        }

        String[] addr = serverAddress.split(":");
        RpcClient client = new RpcClient(addr[0], Integer.parseInt(addr[1]));
        RpcResponse response = client.send(request);
        if (response.getError() != null) {
            throw response.getError();
        } else {
            return response.getResult();
        }
    }
}
