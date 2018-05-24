package org.windwant.rpc.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.windwant.rpc.common.core.annotation.RpcService;
import org.windwant.rpc.common.service.HelloService;

/**
 * Created by windwant on 2016/6/30.
 */
@RpcService(HelloService.class) //注解处理
public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);
    public String hello(String name) {
        String response = String.format("%s say hello!", name);
        logger.info("hello service response: {} say hello", name);
        return response;
    }
}
