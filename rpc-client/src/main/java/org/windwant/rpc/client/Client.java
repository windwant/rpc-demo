package org.windwant.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.windwant.rpc.client.core.ServiceDiscovery;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.windwant.rpc.common.pojo.RpcRequest;
import org.windwant.rpc.common.pojo.RpcResponse;

import java.util.UUID;

/**
 * Created by windwant on 2016/6/30.
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static String clazz = "org.windwant.rpc.common.service.HelloService";
    private static String method = "hello";
    private static String discoveryService = "serviceDiscory";

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        ServiceDiscovery sd = (ServiceDiscovery) ctx.getBean(discoveryService); //获取注册服务
        String sp = sd.discory(); //获取服务地址
        String[] sps = sp.split(":");
        RpcClient client = new RpcClient(sps[0], Integer.parseInt(sps[1]));
        for (int i = 0; i < 10; i++) {
            //构建发送消息
            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setClassName(clazz);
            request.setMethodName(method);
            request.setParameterType(new Class<?>[]{String.class});
            request.setParameters(new Object[]{"lilei" + String.valueOf(i)});
            RpcResponse response = client.send(request);
            logger.info("times: {}, result: {}", i, response.getResult());
            Thread.sleep(500);
        }
    }
}
