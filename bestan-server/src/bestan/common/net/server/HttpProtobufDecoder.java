package bestan.common.net.server;

import java.util.List;

import bestan.common.net.message.MessageFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpProtobufDecoder extends MessageToMessageDecoder<FullHttpRequest>{
	@Override
	protected void decode(ChannelHandlerContext ctx, FullHttpRequest fullRequest, List<Object> out) throws Exception {
		ByteBuf content = fullRequest.content();
		int length = content.readableBytes();
		byte[] bytes = new byte[length];	
		for(int i=0; i<length; i++){
			bytes[i] = content.getByte(i);
		}

		out.add(MessageFactory.getFactory().getMessagePack(bytes));
	}
}
