package org.rpcclient.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.rpcserver.test.service.HelloService;
import org.rpcclient.test.core.RpcProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by aayongche on 2016/6/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest(){
        HelloService helloService = rpcProxy.create(HelloService.class);
        System.out.println(helloService.hello("lilei"));

    }
}
