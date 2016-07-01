package org.rpcclient.test;

import org.rpcclient.test.core.RpcProxy;
import org.rpcserver.test.pojo.RpcRequest;
import org.rpcserver.test.pojo.RpcResponse;
import org.rpcserver.test.service.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.UUID;

/**
 * Created by aayongche on 2016/6/30.
 */
public class Client {
    public static void main(String[] args) throws Exception {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName("org.rpcserver.test.service.HelloService");
        request.setMethodName("hello");
        request.setParameterType(new Class<?>[]{String.class});
        request.setParameters(new Object[]{"lilei"});


        RpcClient client = new RpcClient("localhost", 1099);
        RpcResponse response = client.send(request);
        System.out.println(response);
    }
}
