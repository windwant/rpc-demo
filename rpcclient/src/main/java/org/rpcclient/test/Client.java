package org.rpcclient.test;

import org.rpcclient.test.core.RpcProxy;
import org.rpcclient.test.core.ServiceDiscovery;
import org.rpcserver.test.pojo.RpcRequest;
import org.rpcserver.test.pojo.RpcResponse;
import org.rpcserver.test.service.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.UUID;

/**
 * Created by windwant on 2016/6/30.
 */
public class Client {
    public static void main(String[] args) throws Exception {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName("org.rpcserver.test.service.HelloService");
        request.setMethodName("hello");
        request.setParameterType(new Class<?>[]{String.class});
        request.setParameters(new Object[]{"lilei"});

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        ServiceDiscovery sd = (ServiceDiscovery) ctx.getBean("serviceDiscory");
        String sp = sd.discory();
        String[] sps = sp.split(":");
        RpcClient client = new RpcClient(sps[0], Integer.parseInt(sps[1]));
        RpcResponse response = client.send(request);
        System.out.println(response);
    }
}
