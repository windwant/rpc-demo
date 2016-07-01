package org.rpcserver.test.service.impl;

import org.rpcserver.test.core.annotation.RpcService;
import org.rpcserver.test.service.HelloService;

/**
 * Created by aayongche on 2016/6/30.
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    public String hello(String name) {
        return String.format("%s say hello!", name);
    }
}
