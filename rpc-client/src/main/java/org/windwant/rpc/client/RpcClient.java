package org.windwant.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.windwant.rpc.common.core.codec.RpcDecoder;
import org.windwant.rpc.common.core.codec.RpcEncoder;
import org.windwant.rpc.common.pojo.RpcRequest;
import org.windwant.rpc.common.pojo.RpcResponse;

import javax.annotation.PreDestroy;

/**
 * Created by windwant on 2016/6/30.
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private String host; //服务 host
    private int port; //服务 port

    private RpcResponse response;

    private EventLoopGroup group;

    private Channel client; //当前使用活动通道

    private final Object obj = new Object();

    private final boolean order = true; //是否保持顺序性，接收后再发送下一条消息

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

            bootstrap.connect(host, port).sync();//netty客户端连接服务端并阻塞
        }catch (Exception e){}
    }

    /**
     * 发送消息
     * @param request
     * @return
     * @throws Exception
     */
    public RpcResponse send(RpcRequest request) throws Exception {
        logger.info("client send request: {}", request.toString());
        if(client == null) return null;

        if(!client.isActive()) return null;

        //消息发送
        client.writeAndFlush(request).sync();

        if(order) {
            //阻塞等待回复消息
            synchronized (obj) {
                obj.wait();
            }
        }
        return response;
    }

    /**
     * 消息接收处理
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        logger.info("client received response: {}", msg);
        this.response = msg;

        if(order) {
            //获取消息并唤醒阻塞
            synchronized (obj) {
                obj.notifyAll();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        if(group != null) {
            group.shutdownGracefully();
        }
        //重连
        initClient();
    }

    /**
     * 通道建立触发 保存通道
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        client = ctx.channel();
    }

    @PreDestroy
    private void destroy(){
        if(group != null) {
            group.shutdownGracefully();
        }
    }
}
