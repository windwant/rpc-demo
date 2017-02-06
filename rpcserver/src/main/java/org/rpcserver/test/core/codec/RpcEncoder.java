package org.rpcserver.test.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.rpcserver.test.core.serialize.SerializationUtil;

/**
 * 编码
 * Created by windwant on 2016/6/30.
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)){
            byte[] data = SerializationUtil.serialize(msg); //序列化对象
            out.writeInt(data.length);//写入长度
            out.writeBytes(data);//写入数据
        }
    }
}
