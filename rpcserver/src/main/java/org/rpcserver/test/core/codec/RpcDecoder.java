package org.rpcserver.test.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.rpcserver.test.core.serialize.SerializationUtil;

import java.util.List;

/**
 * Created by aayongche on 2016/6/30.
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 4) return;
        in.markReaderIndex();

        int size = in.readInt();

        if(size < 0){
            ctx.close();
        }

        if(in.readableBytes() < size){
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[size];
        in.readBytes(data);
        Object obj = SerializationUtil.deserialize(data, genericClass);
        out.add(obj);
    }
}
