package org.windwant.rpc.common.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.windwant.rpc.common.core.serialize.SerializationUtil;

import java.util.List;

/**
 * 解码
 * Created by windwant on 2016/6/30.
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 4) return; //初始4个长度为记录数据长度位置
        in.markReaderIndex();//记录开始读取位置

        int size = in.readInt();

        if(size < 0){
            ctx.close();
        }

        if(in.readableBytes() < size){
            in.resetReaderIndex(); //重置回marked index
            return;
        }

        byte[] data = new byte[size];
        in.readBytes(data);
        Object obj = SerializationUtil.deserialize(data, genericClass);
        out.add(obj);
    }
}
