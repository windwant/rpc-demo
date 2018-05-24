package org.windwant.rpc.server.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.windwant.rpc.common.pojo.RpcRequest;
import org.windwant.rpc.common.pojo.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by windwant on 2016/6/30.
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    RpcHandler(Map<String, Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    private BlockingQueue tasks = new ArrayBlockingQueue(100);

    private Executor executor = new ThreadPoolExecutor(3, 5, 0L, TimeUnit.SECONDS, tasks);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        dealTask(ctx, msg);
    }

    /**
     * 将请求逻辑放入业务线程池处理
     * @param ctx
     * @param msg
     */
    private void dealTask(final ChannelHandlerContext ctx, final RpcRequest msg){
        executor.execute(new Runnable() {
            public void run() {
                try {
                    //模拟不同请求需要处理的时间
                    Thread.sleep(ThreadLocalRandom.current().nextInt(5)*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RpcResponse response = new RpcResponse();//构造返回消息
                response.setRequestId(msg.getRequestId());//同一个消息ID
                try {
                    Object rst = handleReuqest(msg);//处理请求消息体 反射调用接口获取结果返回
                    response.setResult(rst);
                } catch (Throwable t) {
                    response.setError(t);
                }

                ctx.writeAndFlush(response); //写出消息
                logger.info("server send response: {}", response.toString());
            }
        });
    }

    /**
     * 反射及调用
     * @param request
     * @return
     * @throws InvocationTargetException
     */
    private Object handleReuqest(RpcRequest request) throws InvocationTargetException {
        logger.info("server accept request: {}", request.toString());

//        return invokeService(request);//java 反射处理
        String className = request.getClassName(); //调用的接口名称
        Object serviceBean = handlerMap.get(className); //根据接口名称获取对象
        if(serviceBean == null) return null;

        //Cglib 处理反射
        FastClass fastClass = FastClass.create(serviceBean.getClass());
        FastMethod fastMethod = fastClass.getMethod(request.getMethodName(), request.getParameterType());
        return fastMethod.invoke(serviceBean, request.getParameters());
    }

    private Object invokeService(RpcRequest request){
        Object serviceBean = handlerMap.get(request.getClassName()); //根据接口名称获取对象
        try {
            if(serviceBean == null) return null;
            Method method = serviceBean.getClass().getMethod(request.getMethodName(), request.getParameterType());
            return method.invoke(serviceBean, request.getParameters());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
