package org.rpcserver.test.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections.MapUtils;
import org.rpcserver.test.core.ServiceRegistry;
import org.rpcserver.test.core.annotation.RpcService;
import org.rpcserver.test.core.codec.RpcDecoder;
import org.rpcserver.test.core.codec.RpcEncoder;
import org.rpcserver.test.pojo.RpcRequest;
import org.rpcserver.test.pojo.RpcResponse;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by windwant on 2016/6/30.
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {
    private String serverAddress;

    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    RpcServer(String serverAddress){
        this.serverAddress = serverAddress;
    }

    RpcServer(String serverAddress, ServiceRegistry serviceRegistry){
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

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
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcDecoder(RpcRequest.class));
                            ch.pipeline().addLast(new RpcEncoder(RpcResponse.class));
                            ch.pipeline().addLast(new RpcHandler(handlerMap)); //service处理
                        }
                    });
            String[] addr = serverAddress.split(":");
            ChannelFuture cf = b.bind(addr[0], Integer.parseInt(addr[1])).sync();
            if(!Objects.isNull(serviceRegistry)){
                serviceRegistry.registry(serverAddress);
            }
            cf.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
