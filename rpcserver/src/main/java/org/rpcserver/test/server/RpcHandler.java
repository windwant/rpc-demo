package org.rpcserver.test.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.rpcserver.test.pojo.RpcRequest;
import org.rpcserver.test.pojo.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by aayongche on 2016/6/30.
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Map<String, Object> handlerMap;

    RpcHandler(Map<String, Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(msg.getRequestId());
        try {
            Object rst = handleReuqest(msg);
            response.setResult(rst);
        } catch (Throwable t){
            response.setError(t);
        }

        ctx.writeAndFlush(response);
    }

    private Object handleReuqest(RpcRequest request) throws InvocationTargetException {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

        Class<?> serverCls = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] ptype = request.getParameterType();
        Object[] ps = request.getParameters();

        FastClass fastClass = FastClass.create(serverCls);
        FastMethod fastMethod = fastClass.getMethod(methodName, ptype);
        return fastMethod.invoke(serviceBean, ps);
    }
}
