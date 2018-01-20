package bestan.common.net.netty;

import bestan.common.datastruct.Pair;
import bestan.common.net.message.DelayedEventHandler;
import bestan.common.net.message.MessageFactory;
import bestan.common.net.nio.Encoder;

import com.google.protobuf.GeneratedMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class NettyOutPutHandler extends ChannelOutboundHandlerAdapter{
	@Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof GeneratedMessage) {
	        int nMsgId = MessageFactory.getFactory().getMsgIdByMessageClass(msg.getClass());
			if(-1 == nMsgId) {
				return;
			}
			
			//通过msgid 来判断 是不是同步  默认全是 0x01 还原等待标志
			byte operation = 0x01;
//			if(Messagetype.MessageType.MSGTYPE_SYNC_BENCHMARK_VALUE < nMsgId) {
//				operation = 0x00;
//			}
			
			byte[] data = Encoder.get().encode((GeneratedMessage) msg, nMsgId, operation);
			
			ByteBuf encoded = ctx.alloc().buffer(data.length);
			encoded.writeBytes(data);
			ctx.writeAndFlush(encoded);
			
//			ByteBuf encoded = Unpooled.wrappedBuffer(data);
//			ctx.writeAndFlush(encoded);
//			encoded.release();
			//.addListener(ChannelFutureListener.CLOSE);
        } else if(msg instanceof Pair) { //db command nio model
        	@SuppressWarnings("unchecked")
			Pair<DelayedEventHandler, Object> entry = (Pair<DelayedEventHandler, Object>) msg;
			entry.first().handleDelayedEvent(entry.second());
        }
    }
}
