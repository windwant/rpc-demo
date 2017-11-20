package org.rpcclient.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.rpcserver.test.core.codec.RpcDecoder;
import org.rpcserver.test.core.codec.RpcEncoder;
import org.rpcserver.test.pojo.RpcRequest;
import org.rpcserver.test.pojo.RpcResponse;

import javax.annotation.PreDestroy;

/**
 * Created by windwant on 2016/6/30.
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private String host;
    private int port;

    private RpcResponse response;

    private EventLoopGroup group;

    private ChannelFuture future;

    private final Object obj = new Object();

    public RpcClient(String host, int port){
        this.host = host;
        this.port = port;
        initClient();
    }

    /**
     * 初始化连接
     */
    private void initClient() {
        group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class)) // 将 RPC 请求进行编码（为了发送请求）
                                    .addLast(new RpcDecoder(RpcResponse.class)) // 将 RPC 响应进行解码（为了处理响应）
                                    .addLast(RpcClient.this); // 使用 RpcClient 发送 RPC 请求 SimpleChannelInboundHandler
                        }
                    });

            future = bootstrap.connect(host, port).sync();
        }catch (Exception e){}
    }

    /**
     * 发送消息
     * @param request
     * @return
     * @throws Exception
     */
    public RpcResponse send(RpcRequest request) throws Exception {
        if(future == null) return null;

        future.channel().writeAndFlush(request).sync();

        synchronized (obj){
            obj.wait();
        }

        if(response != null){
            //future.channel().closeFuture().sync();
        }
        return response;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        this.response = msg;
        synchronized (obj){
            obj.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        if(group != null) {
            group.shutdownGracefully();
        }
    }

    @PreDestroy
    private void destroy(){
        if(group != null) {
            group.shutdownGracefully();
        }
    }
}
