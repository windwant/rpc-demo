package org.rpcserver.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by aayongche on 2016/6/30.
 */
public class RpcBootstrap {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext cxt = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        cxt.start();
    }
}
