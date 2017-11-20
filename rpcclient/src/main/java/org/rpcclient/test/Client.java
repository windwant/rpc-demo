package org.rpcclient.test;

import org.rpcclient.test.core.ServiceDiscovery;
import org.rpcserver.test.pojo.RpcRequest;
import org.rpcserver.test.pojo.RpcResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.UUID;

/**
 * Created by windwant on 2016/6/30.
 */
public class Client {
    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        ServiceDiscovery sd = (ServiceDiscovery) ctx.getBean("serviceDiscory"); //获取注册服务
        String sp = sd.discory(); //获取服务地址
        String[] sps = sp.split(":");
        RpcClient client = new RpcClient(sps[0], Integer.parseInt(sps[1]));
        for (int i = 0; i < 10; i++) {
            //构建发送消息
            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setClassName("org.rpcserver.test.service.HelloService");
            request.setMethodName("hello");
            request.setParameterType(new Class<?>[]{String.class});
            request.setParameters(new Object[]{"lilei" + String.valueOf(i)});
            RpcResponse response = client.send(request);
            System.out.println(response);
            Thread.sleep(500);
        }
    }
}
