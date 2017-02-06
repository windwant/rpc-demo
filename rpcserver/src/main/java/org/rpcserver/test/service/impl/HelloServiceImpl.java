package org.rpcserver.test.service.impl;

import org.rpcserver.test.core.annotation.RpcService;
import org.rpcserver.test.service.HelloService;

/**
 * Created by windwant on 2016/6/30.
 */
@RpcService(HelloService.class) //注解处理
public class HelloServiceImpl implements HelloService {
    public String hello(String name) {
        String response = String.format("%s say hello!", name);
        System.out.println(response);
        return response;
    }
}
