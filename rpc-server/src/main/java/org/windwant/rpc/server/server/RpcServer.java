package org.windwant.rpc.server.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections.MapUtils;
import org.windwant.rpc.common.core.ServiceRegistry;
import org.windwant.rpc.common.core.annotation.RpcService;
import org.windwant.rpc.common.core.codec.RpcDecoder;
import org.windwant.rpc.common.core.codec.RpcEncoder;
import org.windwant.rpc.common.pojo.RpcRequest;
import org.windwant.rpc.common.pojo.RpcResponse;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RPC 服务
 * Created by windwant on 2016/6/30.
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {
    private String serverAddress; //服务地址

    private ServiceRegistry serviceRegistry; //服务注册服务 zookeeper

    private Map<String, Object> handlerMap = new HashMap<String, Object>(); //服务接口

    RpcServer(String serverAddress){
        this.serverAddress = serverAddress;
    }

    RpcServer(String serverAddress, ServiceRegistry serviceRegistry){
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    //ApplicationContextAware 自动注入方法
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //根据注解获取beanMap “@RpcService”
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for(Map.Entry<String, Object> entry: serviceBeanMap.entrySet()){
                //获取处理服务接口名 value
                String interfaceName = entry.getValue().getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, entry.getValue());
            }
        }
    }

    /**
     * netty 启动服务
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(); //处理链接
        EventLoopGroup worker = new NioEventLoopGroup(); //处理业务
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true) //长连接保持
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcDecoder(RpcRequest.class));//请求消息解码
                            ch.pipeline().addLast(new RpcEncoder(RpcResponse.class));//回复消息编码
                            ch.pipeline().addLast(new RpcHandler(handlerMap)); //service处理 SimpleChannelInboundHandler
                        }
                    });
            //绑定地址
            String[] addr = serverAddress.split(":");
            ChannelFuture cf = b.bind(addr[0], Integer.parseInt(addr[1])).sync();
            //注册服务
            if(!Objects.isNull(serviceRegistry)){
                serviceRegistry.registry(serverAddress);
            }
            //阻塞
            cf.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
